package com.petrochina.xnyqt.pims.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.common.context.UserContext;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.DateUtil;
import com.petrochina.xnyqt.pims.common.util.NumberUtil;
import com.petrochina.xnyqt.pims.risk.calculator.IRiskCalculateInterFace;
import com.petrochina.xnyqt.pims.risk.calculator.RiskCalcContextDTO;
import com.petrochina.xnyqt.pims.risk.calculator.RiskCalcResultDTO;
import com.petrochina.xnyqt.pims.risk.config.RiskProperties;
import com.petrochina.xnyqt.pims.risk.dao.RiskAssessmentDAO;
import com.petrochina.xnyqt.pims.risk.dao.RiskFactorDAO;
import com.petrochina.xnyqt.pims.risk.dto.AssessRequestDTO;
import com.petrochina.xnyqt.pims.risk.feign.CorrosionFeignClient;
import com.petrochina.xnyqt.pims.risk.feign.GisFeignClient;
import com.petrochina.xnyqt.pims.risk.feign.dto.CorrosionSegmentDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.GisSegmentDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.InspectionDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.PredictSummaryDTO;
import com.petrochina.xnyqt.pims.risk.model.RiskAssessmentModel;
import com.petrochina.xnyqt.pims.risk.model.RiskFactorModel;
import com.petrochina.xnyqt.pims.risk.service.IRiskAssessInterFace;
import com.petrochina.xnyqt.pims.risk.type.FactorSourceType;
import com.petrochina.xnyqt.pims.risk.type.RiskLevelType;
import com.petrochina.xnyqt.pims.risk.type.RiskMatrixType;
import com.petrochina.xnyqt.pims.risk.type.RiskStatusType;
import com.petrochina.xnyqt.pims.risk.util.RiskInputUtil;
import com.petrochina.xnyqt.pims.risk.vo.AssessmentVO;
import com.petrochina.xnyqt.pims.risk.vo.MatrixLevelVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskFactorGroupVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskFactorVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskInputsVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskLevelVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskMatrixVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskSegmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Name: RiskAssessImpl
 * @Description: 失效风险评价实现：因素预填（OpenFeign 调用内腐蚀/管道位置服务）、输入校验、按配置选择计算模型
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Service
public class RiskAssessImpl implements IRiskAssessInterFace {

    //失效可能性分组
    private static final String GROUP_POF = "POF";

    //失效后果分组
    private static final String GROUP_COF = "COF";

    private final RiskFactorDAO factorDAO;

    private final RiskAssessmentDAO assessmentDAO;

    private final CorrosionFeignClient corrosionClient;

    private final GisFeignClient gisClient;

    private final List<IRiskCalculateInterFace> calculators;

    private final RiskProperties properties;

    private final ObjectMapper objectMapper;

    public RiskAssessImpl(RiskFactorDAO factorDAO, RiskAssessmentDAO assessmentDAO,
                          CorrosionFeignClient corrosionClient, GisFeignClient gisClient,
                          List<IRiskCalculateInterFace> calculators, RiskProperties properties,
                          ObjectMapper objectMapper) {
        this.factorDAO = factorDAO;
        this.assessmentDAO = assessmentDAO;
        this.corrosionClient = corrosionClient;
        this.gisClient = gisClient;
        this.calculators = calculators;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<RiskFactorGroupVO> listFactors() {
        List<RiskFactorModel> factors = loadFactors();
        List<RiskFactorGroupVO> groups = new ArrayList<>();
        groups.add(toGroup(GROUP_POF, "失效可能性因素", factors));
        groups.add(toGroup(GROUP_COF, "失效后果因素", factors));
        return groups;
    }

    @Override
    public RiskMatrixVO getMatrix() {
        RiskMatrixVO vo = new RiskMatrixVO();
        for (int i = 0; i < RiskMatrixType.POF_NAMES.length; i++) {
            vo.getPofLevels().add(level(i + 1, RiskMatrixType.POF_NAMES[i]));
            vo.getCofLevels().add(level(i + 1, RiskMatrixType.COF_NAMES[i]));
            vo.getCells().add(Arrays.stream(RiskMatrixType.CELLS[i]).map(Enum::name).collect(Collectors.toList()));
        }
        for (RiskLevelType type : RiskLevelType.values()) {
            RiskLevelVO levelVO = new RiskLevelVO();
            levelVO.setCode(type.name());
            levelVO.setName(type.getDisplayName());
            levelVO.setColor(type.getColor());
            vo.getRiskLevels().add(levelVO);
        }
        return vo;
    }

    @Override
    public List<RiskSegmentVO> listSegments() {
        List<CorrosionSegmentDTO> segments = unwrap(corrosionClient.listSegments());
        Map<Long, PredictSummaryDTO> summaries = unwrapOrEmpty(
                corrosionClient.summarizeAll(properties.getDefaultEvaluateYears())).stream()
                .collect(Collectors.toMap(PredictSummaryDTO::getSegmentId, Function.identity()));
        Map<Long, RiskAssessmentModel> latest = new LinkedHashMap<>();
        for (RiskAssessmentModel model : assessmentDAO.selectList(new LambdaQueryWrapper<RiskAssessmentModel>()
                .orderByDesc(RiskAssessmentModel::getAssessTime))) {
            latest.putIfAbsent(model.getSegmentId(), model);
        }
        return segments.stream().map(segment -> {
            RiskSegmentVO vo = new RiskSegmentVO();
            vo.setSegmentId(segment.getSegmentId());
            vo.setMine(segment.getMine());
            vo.setPipelineName(segment.getPipelineName());
            vo.setSegmentName(segment.getSegmentName());
            PredictSummaryDTO summary = summaries.get(segment.getSegmentId());
            //可预测管段带出内腐蚀预测摘要
            if (summary != null) {
                vo.setPredDepthMm(summary.getPredDepthMm());
                vo.setDepthRatio(summary.getDepthRatio());
                vo.setCorrosionLimitFlag(summary.getLimitFlag());
            }
            RiskAssessmentModel assessment = latest.get(segment.getSegmentId());
            String status = assessment == null ? RiskStatusType.NOT_ASSESSED.name() : assessment.getStatus();
            vo.setStatus(status);
            vo.setStatusName(RiskStatusType.nameOf(status));
            if (assessment != null) {
                vo.setPofLevel(assessment.getPofLevel());
                vo.setCofLevel(assessment.getCofLevel());
                vo.setRiskLevel(assessment.getRiskLevel());
                vo.setRiskLevelName(RiskLevelType.nameOf(assessment.getRiskLevel()));
                vo.setLastAssessTime(assessment.getAssessTime().format(DateUtil.DATE_TIME_FORMAT));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public RiskInputsVO getInputs(Long segmentId, Double evaluateYears) {
        double years = evaluateYears != null ? evaluateYears : properties.getDefaultEvaluateYears();
        RiskInputsVO vo = new RiskInputsVO();
        vo.setSegmentId(segmentId);
        Map<String, Object> values = new LinkedHashMap<>();
        ApiResult<GisSegmentDTO> gis = gisClient.getSegment(segmentId);
        if (gis.isSuccess() && gis.getData() != null) {
            fillFromGis(values, gis.getData());
        } else {
            vo.getMessages().add("管段数据未能带入：" + gis.getMsg());
        }
        PredictSummaryDTO summary = unwrapOrEmpty(corrosionClient.summarizeAll(years)).stream()
                .filter(item -> segmentId.equals(item.getSegmentId())).findFirst().orElse(null);
        if (summary != null) {
            values.put("predDepthRatio", NumberUtil.round(summary.getDepthRatio() * 100, 1));
            values.put("predDepthMm", summary.getPredDepthMm());
            values.put("remainingWallMm", summary.getRemainingWallMm());
        } else {
            vo.getMessages().add("该管段不满足内腐蚀预测条件，预测类因素需人工录入");
        }
        fillFromInspections(values, unwrapOrEmpty(corrosionClient.listInspections(segmentId)));
        //只返回已定义且启用的因素
        List<String> codes = loadFactors().stream().map(RiskFactorModel::getFactorCode).collect(Collectors.toList());
        values.keySet().retainAll(codes);
        vo.setValues(values);
        return vo;
    }

    @Override
    public AssessmentVO assess(AssessRequestDTO request) {
        Map<String, RiskFactorModel> factors = loadFactors().stream()
                .collect(Collectors.toMap(RiskFactorModel::getFactorCode, Function.identity(), (a, b) -> a,
                        LinkedHashMap::new));
        Map<String, Object> inputs = RiskInputUtil.normalizeInputs(request.getInputs(), factors);
        ApiResult<GisSegmentDTO> gis = gisClient.getSegment(request.getSegmentId());
        if (!gis.isSuccess() || gis.getData() == null) {
            throw new BusinessException("管段不存在或管道位置服务不可用");
        }
        double years = request.getEvaluateYears() != null ? request.getEvaluateYears()
                : properties.getDefaultEvaluateYears();
        IRiskCalculateInterFace calculator = calculators.stream()
                .filter(item -> item.getModelCode().equals(properties.getModelCode())).findFirst()
                .orElseThrow(() -> new BusinessException("未找到失效风险计算模型：" + properties.getModelCode()));
        RiskCalcContextDTO context = new RiskCalcContextDTO();
        context.setSegmentId(request.getSegmentId());
        context.setEvaluateYears(years);
        context.setInputs(inputs);
        RiskCalcResultDTO result = calculateSafely(calculator, context);
        RiskAssessmentModel model = new RiskAssessmentModel();
        model.setAssessmentId(UUID.randomUUID().toString());
        model.setSegmentId(request.getSegmentId());
        model.setSegmentName(gis.getData().getSegmentName());
        model.setEvaluateYears(years);
        model.setModelCode(calculator.getModelCode());
        model.setStatus(result.getStatus());
        model.setPofLevel(result.getPofLevel());
        model.setCofLevel(result.getCofLevel());
        model.setPofValue(result.getPofValue());
        model.setCofValue(result.getCofValue());
        model.setRiskLevel(result.getRiskLevel());
        model.setMessage(result.getMessage());
        model.setInputsJson(toJson(inputs));
        model.setOperator(UserContext.getOperatorName());
        model.setAssessTime(LocalDateTime.now());
        assessmentDAO.insert(model);
        return toAssessmentVO(model);
    }

    @Override
    public List<AssessmentVO> listAssessments(Long segmentId) {
        LambdaQueryWrapper<RiskAssessmentModel> wrapper = new LambdaQueryWrapper<RiskAssessmentModel>()
                .eq(segmentId != null, RiskAssessmentModel::getSegmentId, segmentId)
                .orderByDesc(RiskAssessmentModel::getAssessTime)
                .last("LIMIT " + properties.getHistoryLimit());
        return assessmentDAO.selectList(wrapper).stream().map(this::toAssessmentVO).collect(Collectors.toList());
    }

    /**
     * @Name: calculateSafely
     * @Description: 调用计算模型；有失效可能性与后果等级时由风险矩阵确定风险等级
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: IRiskCalculateInterFace calculator 计算模型, RiskCalcContextDTO context 上下文
     * @Return: RiskCalcResultDTO 计算结果
     */
    private RiskCalcResultDTO calculateSafely(IRiskCalculateInterFace calculator, RiskCalcContextDTO context) {
        RiskCalcResultDTO result;
        try {
            result = calculator.calculate(context);
        } catch (RuntimeException e) {
            log.error("失效风险计算失败：管段 {}", context.getSegmentId(), e);
            result = new RiskCalcResultDTO();
            result.setStatus(RiskStatusType.FAILED.name());
            result.setMessage("失效风险计算失败，请检查输入后重试");
        }
        if (result.getRiskLevel() == null && result.getPofLevel() != null && result.getCofLevel() != null) {
            result.setRiskLevel(RiskMatrixType.levelOf(result.getPofLevel(), result.getCofLevel()).name());
        }
        return result;
    }

    private void fillFromGis(Map<String, Object> values, GisSegmentDTO segment) {
        values.put("wallThicknessMm", segment.getWallThicknessMm());
        values.put("outerDiameterMm", segment.getOuterDiameterMm());
        values.put("designPressureMpa", segment.getDesignPressureMpa());
        values.put("materialGrade", segment.getMaterialGrade());
        values.put("mediumType", segment.getMediumType());
        //服役年限按投运至今计算
        if (segment.getCommissionDate() != null) {
            values.put("serviceYears", NumberUtil.round(
                    DateUtil.yearsBetween(segment.getCommissionDate(), LocalDate.now()), 1));
        }
        values.values().removeIf(Objects::isNull);
    }

    private void fillFromInspections(Map<String, Object> values, List<InspectionDTO> inspections) {
        //按内检日期倒序，各字段取最近一条非空值
        List<InspectionDTO> sorted = new ArrayList<>(inspections);
        sorted.sort((a, b) -> compareDateDesc(a.getInspectionDate(), b.getInspectionDate()));
        for (InspectionDTO inspection : sorted) {
            putIfAbsent(values, "operatingPressureMpa", inspection.getOperatingPressureMpa());
            putIfAbsent(values, "h2sMolPct", inspection.getH2sMolPct());
            putIfAbsent(values, "co2MolPct", inspection.getCo2MolPct());
            putIfAbsent(values, "inhibitorDosing", inspection.getInhibitorDosing());
            putIfAbsent(values, "piggingCycleDays", inspection.getPiggingCycleDays());
        }
    }

    private int compareDateDesc(LocalDate a, LocalDate b) {
        if (a == null || b == null) {
            return a == null ? (b == null ? 0 : 1) : -1;
        }
        return b.compareTo(a);
    }

    private void putIfAbsent(Map<String, Object> values, String code, Object value) {
        if (value != null && !values.containsKey(code)) {
            values.put(code, value);
        }
    }

    private List<RiskFactorModel> loadFactors() {
        return factorDAO.selectList(new LambdaQueryWrapper<RiskFactorModel>()
                .eq(RiskFactorModel::getEnabled, true)
                .orderByAsc(RiskFactorModel::getGroupCode, RiskFactorModel::getSortOrder));
    }

    private RiskFactorGroupVO toGroup(String groupCode, String groupName, List<RiskFactorModel> factors) {
        RiskFactorGroupVO group = new RiskFactorGroupVO();
        group.setGroupCode(groupCode);
        group.setGroupName(groupName);
        group.setFactors(factors.stream().filter(factor -> groupCode.equals(factor.getGroupCode())).map(factor -> {
            RiskFactorVO vo = new RiskFactorVO();
            vo.setCode(factor.getFactorCode());
            vo.setName(factor.getFactorName());
            vo.setUnit(factor.getUnit());
            vo.setInputType(factor.getInputType());
            vo.setSource(factor.getSource());
            vo.setSourceName(FactorSourceType.nameOf(factor.getSource()));
            vo.setOptions(RiskInputUtil.TYPE_SELECT.equals(factor.getInputType())
                    ? RiskInputUtil.parseOptions(factor.getOptionsJson()) : null);
            vo.setRequired(Boolean.TRUE.equals(factor.getRequired()));
            vo.setMin(factor.getMinValue());
            vo.setMax(factor.getMaxValue());
            return vo;
        }).collect(Collectors.toList()));
        return group;
    }

    private MatrixLevelVO level(int level, String name) {
        MatrixLevelVO vo = new MatrixLevelVO();
        vo.setLevel(level);
        vo.setName(name);
        return vo;
    }

    private AssessmentVO toAssessmentVO(RiskAssessmentModel model) {
        AssessmentVO vo = new AssessmentVO();
        vo.setAssessmentId(model.getAssessmentId());
        vo.setSegmentId(model.getSegmentId());
        vo.setSegmentName(model.getSegmentName());
        vo.setAssessTime(model.getAssessTime().format(DateUtil.DATE_TIME_FORMAT));
        vo.setEvaluateYears(model.getEvaluateYears());
        vo.setModelCode(model.getModelCode());
        vo.setStatus(model.getStatus());
        vo.setStatusName(RiskStatusType.nameOf(model.getStatus()));
        vo.setPofLevel(model.getPofLevel());
        vo.setCofLevel(model.getCofLevel());
        vo.setPofValue(model.getPofValue());
        vo.setCofValue(model.getCofValue());
        vo.setRiskLevel(model.getRiskLevel());
        vo.setRiskLevelName(RiskLevelType.nameOf(model.getRiskLevel()));
        vo.setMessage(model.getMessage());
        vo.setOperator(model.getOperator());
        vo.setInputs(fromJson(model.getInputsJson()));
        return vo;
    }

    private String toJson(Map<String, Object> inputs) {
        try {
            return objectMapper.writeValueAsString(inputs);
        } catch (JsonProcessingException e) {
            throw new BusinessException("输入快照保存失败");
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    private <T> T unwrap(ApiResult<T> result) {
        if (result == null || !result.isSuccess()) {
            throw new BusinessException(result == null ? "远程服务无响应" : result.getMsg());
        }
        return result.getData();
    }

    private <T> List<T> unwrapOrEmpty(ApiResult<List<T>> result) {
        return result != null && result.isSuccess() && result.getData() != null
                ? result.getData() : Collections.emptyList();
    }
}
