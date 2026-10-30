package com.petrochina.xnyqt.pims.corrosion.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petrochina.xnyqt.pims.common.context.UserContext;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.DateUtil;
import com.petrochina.xnyqt.pims.common.util.NumberUtil;
import com.petrochina.xnyqt.pims.corrosion.algorithm.PowerLawCorrosionAlgorithm;
import com.petrochina.xnyqt.pims.corrosion.algorithm.FixedPowerLawModel;
import com.petrochina.xnyqt.pims.corrosion.config.CorrosionModelProperties;
import com.petrochina.xnyqt.pims.corrosion.dao.CorrosionPredictLogDAO;
import com.petrochina.xnyqt.pims.corrosion.dto.PredictRequestDTO;
import com.petrochina.xnyqt.pims.corrosion.dto.SegmentAnchorDTO;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionInspectionModel;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionPredictLogModel;
import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionParameterInterFace;
import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionPredictInterFace;
import com.petrochina.xnyqt.pims.corrosion.type.LimitFlagType;
import com.petrochina.xnyqt.pims.corrosion.vo.CurvePointVO;
import com.petrochina.xnyqt.pims.corrosion.vo.InspectionPointVO;
import com.petrochina.xnyqt.pims.corrosion.vo.ModelParameterVO;
import com.petrochina.xnyqt.pims.corrosion.vo.PredictSummaryVO;
import com.petrochina.xnyqt.pims.corrosion.vo.PredictionVO;
import com.petrochina.xnyqt.pims.corrosion.vo.ScenarioResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Name: CorrosionPredictImpl
 * @Description: 内腐蚀固定参数预测：t 从投运日起算；深度达到壁厚即截断
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Service
public class CorrosionPredictImpl implements ICorrosionPredictInterFace {

    //曲线最长延伸年数
    private static final double MAX_CURVE_YEARS = 50.0;

    private final ICorrosionParameterInterFace parameterService;

    private final CorrosionPredictLogDAO predictLogDAO;

    private final CorrosionModelProperties properties;

    private final ObjectMapper objectMapper;

    public CorrosionPredictImpl(ICorrosionParameterInterFace parameterService, CorrosionPredictLogDAO predictLogDAO,
                                CorrosionModelProperties properties, ObjectMapper objectMapper) {
        this.parameterService = parameterService;
        this.predictLogDAO = predictLogDAO;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public PredictionVO predict(PredictRequestDTO request) {
        SegmentAnchorDTO anchor = parameterService.evaluateAnchor(request.getSegmentId());
        FixedPowerLawModel fixed = FixedPowerLawModel.require(anchor.getSegment().getSegmentName());
        fixed.checkN(request.getScenarioN());
        double mainN = fixed.getN();
        ModelParameterVO parameter = parameterService.buildParameter(anchor, mainN);
        LocalDate anchorDate = anchor.getAnchor().getInspectionDate();
        LocalDate targetDate = resolveTargetDate(request, anchorDate);
        double deltaYears = DateUtil.yearsBetween(anchorDate, targetDate);
        double curveEnd = resolveCurveEnd(request, deltaYears, anchorDate);
        PredictionVO vo = new PredictionVO();
        vo.setCalcId(UUID.randomUUID().toString());
        vo.setCalcTime(LocalDateTime.now().format(DateUtil.DATE_TIME_FORMAT));
        vo.setParameter(parameter);
        vo.setTargetDate(targetDate);
        vo.setDeltaYears(NumberUtil.round(deltaYears, 4));
        vo.setMain(calculateScenario(anchor, mainN, deltaYears, curveEnd));
        //保留 scenarios 数组结构，不再执行敏感性情景。
        vo.setInspectionPoints(buildInspectionPoints(anchor));
        vo.setWarnings(buildWarnings(anchor, vo.getMain(), deltaYears, mainN));
        saveLog(vo, anchor, request);
        return vo;
    }

    @Override
    public List<PredictSummaryVO> summarizeAll(double yearsAfterIli) {
        return parameterService.evaluateAllAnchors().stream()
                .filter(SegmentAnchorDTO::isPredictable)
                .map(anchor -> {
                    ScenarioResultVO result = calculateScenario(anchor, FixedPowerLawModel.require(anchor.getSegment().getSegmentName()).getN(),
                            DateUtil.yearsBetween(anchor.getAnchor().getInspectionDate(),
                                    DateUtil.plusYears(anchor.getAnchor().getInspectionDate(), yearsAfterIli)), -1);
                    PredictSummaryVO summary = new PredictSummaryVO();
                    summary.setSegmentId(anchor.getSegment().getSegmentId());
                    summary.setSegmentName(anchor.getSegment().getSegmentName());
                    summary.setTargetDate(DateUtil.plusYears(anchor.getAnchor().getInspectionDate(), yearsAfterIli));
                    summary.setPredDepthMm(result.getPredDepthMm());
                    summary.setDepthRatio(result.getDepthRatio());
                    summary.setRemainingWallMm(result.getRemainingWallMm());
                    summary.setLimitFlag(result.getLimitFlag());
                    summary.setLimitFlagName(result.getLimitFlagName());
                    return summary;
                })
                .collect(Collectors.toList());
    }

    /**
     * @Name: resolveTargetDate
     * @Description: 解析目标日期：指定日期须晚于最近内检；否则按内检后年数推算
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: PredictRequestDTO request 请求, LocalDate anchorDate 最近内检日期
     * @Return: LocalDate 目标日期
     */
    private LocalDate resolveTargetDate(PredictRequestDTO request, LocalDate anchorDate) {
        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(anchorDate)) {
                throw new BusinessException("目标日期须晚于最近内检日期 " + DateUtil.format(anchorDate));
            }
            return request.getTargetDate();
        }
        double years = request.getYearsAfterIli() != null ? request.getYearsAfterIli()
                : properties.getDefaultYearsAfterIli();
        return DateUtil.plusYears(anchorDate, years);
    }

    private double resolveCurveEnd(PredictRequestDTO request, double deltaYears, LocalDate anchorDate) {
        if (request.getCurveEndYears() != null) {
            return Math.max(request.getCurveEndYears(), deltaYears);
        }
        double sinceIli = DateUtil.yearsBetween(anchorDate, LocalDate.now());
        return Math.min(MAX_CURVE_YEARS, Math.max(Math.max(deltaYears + 3, sinceIli + 5), 10));
    }

    /**
     * @Name: calculateScenario
     * @Description: 计算管段固定参数结果；达到名义壁厚时越界并截断
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: SegmentAnchorDTO anchor 锚点, double n 指数, double deltaYears Δt, double curveEnd 曲线延伸年数（小于0不生成曲线）
     * @Return: ScenarioResultVO 情景结果
     */
    private ScenarioResultVO calculateScenario(SegmentAnchorDTO anchor, double n, double deltaYears, double curveEnd) {
        double t = anchor.getAnchorYears();
        double wall = anchor.getAnchor().getWallThicknessMm();
        FixedPowerLawModel fixed = FixedPowerLawModel.require(anchor.getSegment().getSegmentName());
        double k = fixed.getK();
        double depth = PowerLawCorrosionAlgorithm.depthAt(k, t + deltaYears, n);
        double ratio = depth / wall;
        LimitFlagType flag = ratio >= 1.0 ? LimitFlagType.MODEL_OUT_OF_RANGE
                : ratio >= properties.getAttentionRatio() ? LimitFlagType.ATTENTION : LimitFlagType.NORMAL;
        ScenarioResultVO vo = new ScenarioResultVO();
        vo.setN(n);
        vo.setKValue(k);
        //越界后不再给出超过壁厚的物理深度
        vo.setPredDepthMm(Math.min(wall, NumberUtil.round(Math.min(depth, wall), 3)));
        vo.setDepthRatio(NumberUtil.round(Math.min(ratio, 1.0), 4));
        vo.setRemainingWallMm(NumberUtil.round(Math.max(wall - depth, 0.0), 3));
        vo.setLimitFlag(flag.name());
        vo.setLimitFlagName(flag.getDisplayName());
        vo.setOutOfRangeDate(fixed.cutoffDate(anchor.getSegment().getCommissionDate(), wall));
        vo.setCurve(curveEnd < 0 ? null : buildCurve(anchor, k, n, deltaYears, curveEnd));
        return vo;
    }

    private List<CurvePointVO> buildCurve(SegmentAnchorDTO anchor, double k, double n, double deltaYears,
                                          double curveEnd) {
        double t = anchor.getAnchorYears();
        double wall = anchor.getAnchor().getWallThicknessMm();
        LocalDate commission = anchor.getSegment().getCommissionDate();
        TreeSet<Double> times = new TreeSet<>();
        for (double x = 0; x < t + curveEnd; x += 0.5) {
            times.add(x);
        }
        times.add(t);
        times.add(t + deltaYears);
        times.add(t + curveEnd);
        List<CurvePointVO> curve = new ArrayList<>();
        for (Double time : times) {
            double depth = PowerLawCorrosionAlgorithm.depthAt(k, time, n);
            //达到名义壁厚：补充越界点后终止外推
            if (depth >= wall) {
                double wallTime = PowerLawCorrosionAlgorithm.timeToReach(k, wall, n);
                curve.add(point(commission, wallTime, wall));
                break;
            }
            curve.add(point(commission, time, depth));
        }
        return curve;
    }

    private CurvePointVO point(LocalDate commission, double time, double depth) {
        CurvePointVO point = new CurvePointVO();
        point.setDate(DateUtil.plusYears(commission, time));
        point.setTYears(time);
        point.setDepthMm(depth);
        return point;
    }

    private List<InspectionPointVO> buildInspectionPoints(SegmentAnchorDTO anchor) {
        List<InspectionPointVO> points = new ArrayList<>();
        LocalDate commission = anchor.getSegment().getCommissionDate();
        for (CorrosionInspectionModel inspection : anchor.getInspections()) {
            //只输出日期、深度、壁厚齐全的实测点
            if (inspection.getInspectionDate() == null || inspection.getMaxDepthPct() == null
                    || inspection.getWallThicknessMm() == null) {
                continue;
            }
            InspectionPointVO point = new InspectionPointVO();
            point.setDate(inspection.getInspectionDate());
            point.setTYears(NumberUtil.round(DateUtil.yearsBetween(commission, inspection.getInspectionDate()), 4));
            point.setDepthMm(NumberUtil.round(PowerLawCorrosionAlgorithm.initialDepth(
                    inspection.getWallThicknessMm(), inspection.getMaxDepthPct()), 3));
            point.setDepthPct(inspection.getMaxDepthPct());
            point.setIsAnchor(inspection.getInspectionId().equals(anchor.getAnchor().getInspectionId()));
            points.add(point);
        }
        return points;
    }

    private List<String> buildWarnings(SegmentAnchorDTO anchor, ScenarioResultVO main, double deltaYears, double n) {
        List<String> warnings = new ArrayList<>();
        FixedPowerLawModel fixed = FixedPowerLawModel.require(anchor.getSegment().getSegmentName());
        double rawRatio = PowerLawCorrosionAlgorithm.depthAt(fixed.getK(), anchor.getAnchorYears() + deltaYears,
                fixed.getN()) / anchor.getAnchor().getWallThicknessMm();
        warnings.add("使用用户指定固定参数 d=k*t^n；内检实测仅作拟合比较，不能作为独立验证；不执行敏感性情景");
        if (LimitFlagType.MODEL_OUT_OF_RANGE.name().equals(main.getLimitFlag())) {
            warnings.add(String.format("预测深度已达到名义壁厚（模型外推值为壁厚的 %.1f%%），返回 MODEL_OUT_OF_RANGE 并停止物理外推；"
                    + "该结果为模型越界/数据核验提示，不代表腐蚀深度可超过壁厚。", rawRatio * 100));
        } else if (LimitFlagType.ATTENTION.name().equals(main.getLimitFlag())) {
            warnings.add(String.format("预测深度达到名义壁厚的 %.1f%%（≥%.0f%%），建议优先安排复检核实。",
                    rawRatio * 100, properties.getAttentionRatio() * 100));
        }
        LocalDate anchorDate = anchor.getAnchor().getInspectionDate();
        double sinceIli = DateUtil.yearsBetween(anchorDate, LocalDate.now());
        //最近内检过旧时提示数据时效性
        if (sinceIli > properties.getStaleInspectionYears()) {
            warnings.add(String.format("最近一次有效内检（%s）距今 %.1f 年，外推跨度较大，建议补充内检数据。",
                    DateUtil.format(anchorDate), sinceIli));
        }
        warnings.addAll(anchor.getNotes());
        return warnings;
    }

    private void saveLog(PredictionVO vo, SegmentAnchorDTO anchor, PredictRequestDTO request) {
        CorrosionPredictLogModel logModel = new CorrosionPredictLogModel();
        logModel.setCalcId(vo.getCalcId());
        logModel.setSegmentId(anchor.getSegment().getSegmentId());
        logModel.setModelVersion(properties.getModelVersion());
        logModel.setExponentN(vo.getMain().getN());
        logModel.setCoefficientK(vo.getMain().getKValue());
        logModel.setD0Mm(anchor.getD0Mm());
        logModel.setAnchorYears(anchor.getAnchorYears());
        logModel.setAnchorDate(anchor.getAnchor().getInspectionDate());
        logModel.setTargetDate(vo.getTargetDate());
        logModel.setDeltaYears(vo.getDeltaYears());
        logModel.setPredDepthMm(vo.getMain().getPredDepthMm());
        logModel.setDepthRatio(vo.getMain().getDepthRatio());
        logModel.setLimitFlag(vo.getMain().getLimitFlag());
        logModel.setOperator(UserContext.getOperatorName());
        logModel.setCalcTime(LocalDateTime.now());
        try {
            logModel.setRequestJson(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            logModel.setRequestJson(null);
        }
        predictLogDAO.insert(logModel);
    }
}
