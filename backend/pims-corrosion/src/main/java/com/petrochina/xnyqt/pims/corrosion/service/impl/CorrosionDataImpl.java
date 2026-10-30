package com.petrochina.xnyqt.pims.corrosion.service.impl;

import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.DateUtil;
import com.petrochina.xnyqt.pims.common.util.NumberUtil;
import com.petrochina.xnyqt.pims.corrosion.algorithm.PowerLawCorrosionAlgorithm;
import com.petrochina.xnyqt.pims.corrosion.algorithm.FixedPowerLawModel;
import com.petrochina.xnyqt.pims.corrosion.config.CorrosionModelProperties;
import com.petrochina.xnyqt.pims.corrosion.dao.CorrosionInspectionDAO;
import com.petrochina.xnyqt.pims.corrosion.dao.CorrosionSegmentDAO;
import com.petrochina.xnyqt.pims.corrosion.dto.SegmentAnchorDTO;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionInspectionModel;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionSegmentModel;
import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionDataInterFace;
import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionParameterInterFace;
import com.petrochina.xnyqt.pims.corrosion.type.TrendDirectionType;
import com.petrochina.xnyqt.pims.corrosion.util.InspectionExcelReader;
import com.petrochina.xnyqt.pims.corrosion.vo.ApparentExponentVO;
import com.petrochina.xnyqt.pims.corrosion.vo.CorrosionOverviewVO;
import com.petrochina.xnyqt.pims.corrosion.vo.CorrosionSegmentVO;
import com.petrochina.xnyqt.pims.corrosion.vo.InspectionImportResultVO;
import com.petrochina.xnyqt.pims.corrosion.vo.InspectionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @Name: CorrosionDataImpl
 * @Description: 内腐蚀数据业务实现
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Service
public class CorrosionDataImpl implements ICorrosionDataInterFace {

    private final CorrosionSegmentDAO segmentDAO;

    private final CorrosionInspectionDAO inspectionDAO;

    private final ICorrosionParameterInterFace parameterService;

    private final CorrosionModelProperties properties;

    public CorrosionDataImpl(CorrosionSegmentDAO segmentDAO, CorrosionInspectionDAO inspectionDAO,
                             ICorrosionParameterInterFace parameterService, CorrosionModelProperties properties) {
        this.segmentDAO = segmentDAO;
        this.inspectionDAO = inspectionDAO;
        this.parameterService = parameterService;
        this.properties = properties;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionImportResultVO importInspections(InputStream input, String batchName) {
        InspectionImportResultVO result = new InspectionImportResultVO();
        List<CorrosionInspectionModel> rows = new InspectionExcelReader().read(input, result.getMessages());
        if (rows.isEmpty()) {
            throw new BusinessException("未读取到有效数据行");
        }
        //按“气矿—管线—管段”归并物理管段
        Map<String, List<CorrosionInspectionModel>> groups = new LinkedHashMap<>();
        for (CorrosionInspectionModel row : rows) {
            String key = row.getMine() + "|" + row.getPipelineName() + "|" + row.getSegmentName();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        inspectionDAO.delete(null);
        segmentDAO.delete(null);
        LocalDateTime now = LocalDateTime.now();
        for (List<CorrosionInspectionModel> group : groups.values()) {
            group.sort(CorrosionParameterImpl.INSPECTION_ORDER);
            CorrosionSegmentModel segment = buildSegment(group, batchName, result.getMessages());
            segment.setUpdateTime(now);
            segmentDAO.insert(segment);
            for (CorrosionInspectionModel row : group) {
                row.setSegmentId(segment.getSegmentId());
                row.setImportBatch(batchName);
                row.setCreateTime(now);
                inspectionDAO.insert(row);
            }
        }
        result.setTotal(rows.size() + result.getMessages().size());
        result.setImported(rows.size());
        result.setSegmentCount(groups.size());
        int paramCount = parameterService.refreshParameterLibrary();
        result.getMessages().add("已刷新幂律参数库：" + paramCount + " 个固定参数可预测管段");
        log.info("内腐蚀数据导入完成：批次 {}，记录 {}，管段 {}", batchName, rows.size(), groups.size());
        return result;
    }

    @Override
    public long countInspections() {
        return inspectionDAO.selectCount(null);
    }

    @Override
    public List<CorrosionSegmentVO> listSegments() {
        return parameterService.evaluateAllAnchors().stream().map(this::toSegmentVO).collect(Collectors.toList());
    }

    @Override
    public List<InspectionVO> listInspections(Long segmentId) {
        SegmentAnchorDTO anchor = parameterService.evaluateAnchor(segmentId);
        return anchor.getInspections().stream()
                .map(inspection -> toInspectionVO(anchor, inspection))
                .collect(Collectors.toList());
    }

    @Override
    public CorrosionOverviewVO getOverview() {
        List<SegmentAnchorDTO> anchors = parameterService.evaluateAllAnchors();
        List<CorrosionInspectionModel> inspections = anchors.stream()
                .flatMap(anchor -> anchor.getInspections().stream()).collect(Collectors.toList());
        List<Double> ks = anchors.stream().filter(SegmentAnchorDTO::isPredictable)
                .map(anchor -> baseK(anchor)).sorted().collect(Collectors.toList());
        CorrosionOverviewVO vo = new CorrosionOverviewVO();
        vo.setSegmentCount(anchors.size());
        vo.setInspectionCount(inspections.size());
        vo.setPredictableCount(ks.size());
        vo.setMedianK(NumberUtil.round(median(ks), 4));
        vo.setMaxDepthPct(inspections.stream().map(CorrosionInspectionModel::getMaxDepthPct).filter(Objects::nonNull)
                .max(Double::compare).orElse(null));
        vo.setLatestInspectionDate(inspections.stream().map(CorrosionInspectionModel::getInspectionDate)
                .filter(Objects::nonNull).max(LocalDate::compareTo).orElse(null));
        vo.setModelName(properties.getModelName());
        vo.setModelVersion(properties.getModelVersion());
        vo.setBaseN(null); //三管段各自使用固定 n，无统一基准指数
        return vo;
    }

    @Override
    public List<ApparentExponentVO> listApparentExponents() {
        Map<Long, List<CorrosionInspectionModel>> bySource = new TreeMap<>();
        for (SegmentAnchorDTO anchor : parameterService.evaluateAllAnchors()) {
            for (CorrosionInspectionModel inspection : anchor.getInspections()) {
                //仅取日期、深度、壁厚、投运日期齐全的记录
                if (inspection.getInspectionDate() != null && inspection.getMaxDepthPct() != null
                        && inspection.getMaxDepthPct() > 0 && inspection.getWallThicknessMm() != null
                        && anchor.getSegment().getCommissionDate() != null) {
                    bySource.computeIfAbsent(inspection.getSourceSegmentId(), k -> new ArrayList<>()).add(inspection);
                }
            }
        }
        List<ApparentExponentVO> result = new ArrayList<>();
        for (List<CorrosionInspectionModel> records : bySource.values()) {
            records.sort(CorrosionParameterImpl.INSPECTION_ORDER);
            for (int i = 1; i < records.size(); i++) {
                ApparentExponentVO vo = toApparentExponent(records.get(i - 1), records.get(i));
                //两期壁厚不一致时百分比不可直接比较，跳过
                if (vo != null) {
                    result.add(vo);
                }
            }
        }
        return result;
    }

    private CorrosionSegmentModel buildSegment(List<CorrosionInspectionModel> group, String batchName,
                                               List<String> messages) {
        CorrosionInspectionModel first = group.get(0);
        CorrosionSegmentModel segment = new CorrosionSegmentModel();
        segment.setSegmentId(group.stream().map(CorrosionInspectionModel::getSourceSegmentId).min(Long::compare).get());
        segment.setMine(first.getMine());
        segment.setPipelineName(first.getPipelineName());
        segment.setSegmentName(first.getSegmentName());
        segment.setSourceSegmentIds(group.stream().map(CorrosionInspectionModel::getSourceSegmentId).distinct().sorted()
                .map(String::valueOf).collect(Collectors.joining(",")));
        segment.setImportBatch(batchName);
        List<LocalDate> commissionDates = group.stream().map(CorrosionInspectionModel::getCommissionDate)
                .filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());
        //投运日期不一致时取最早值并提示
        if (commissionDates.size() > 1) {
            messages.add(first.getSegmentName() + " 存在多个投运日期 " + commissionDates + "，已取最早值");
        }
        segment.setCommissionDate(commissionDates.isEmpty() ? null : commissionDates.get(0));
        //作业区、外径、壁厚取最近一条非空值
        for (CorrosionInspectionModel row : group) {
            segment.setWorkArea(row.getWorkArea() != null ? row.getWorkArea() : segment.getWorkArea());
            segment.setOuterDiameterMm(row.getOuterDiameterMm() != null ? row.getOuterDiameterMm()
                    : segment.getOuterDiameterMm());
            segment.setWallThicknessMm(row.getWallThicknessMm() != null ? row.getWallThicknessMm()
                    : segment.getWallThicknessMm());
        }
        return segment;
    }

    private CorrosionSegmentVO toSegmentVO(SegmentAnchorDTO anchor) {
        CorrosionSegmentModel segment = anchor.getSegment();
        CorrosionSegmentVO vo = new CorrosionSegmentVO();
        vo.setSegmentId(segment.getSegmentId());
        vo.setMine(segment.getMine());
        vo.setWorkArea(segment.getWorkArea());
        vo.setPipelineName(segment.getPipelineName());
        vo.setSegmentName(segment.getSegmentName());
        vo.setCommissionDate(segment.getCommissionDate());
        vo.setWallThicknessMm(segment.getWallThicknessMm());
        vo.setSourceSegmentIds(segment.getSourceSegmentIds());
        vo.setInspectionCount(anchor.getInspections().size());
        vo.setPredictable(anchor.isPredictable());
        vo.setGrade(anchor.getGrade().name());
        vo.setGradeName(anchor.getGrade().getDisplayName());
        vo.setNote(anchor.getNotes().isEmpty() ? null : String.join("；", anchor.getNotes()));
        //可计算管段输出基准参数
        if (anchor.getAnchor() != null) {
            vo.setLatestInspectionDate(anchor.getAnchor().getInspectionDate());
            vo.setLatestMaxDepthPct(anchor.getAnchor().getMaxDepthPct());
            vo.setD0Mm(NumberUtil.round(anchor.getD0Mm(), 3));
            vo.setTYears(NumberUtil.round(anchor.getAnchorYears(), 4));
            if (anchor.isPredictable()) { vo.setKValue(baseK(anchor)); }
        }
        return vo;
    }

    private InspectionVO toInspectionVO(SegmentAnchorDTO anchor, CorrosionInspectionModel m) {
        InspectionVO vo = new InspectionVO();
        vo.setInspectionId(m.getInspectionId());
        vo.setSourceSegmentId(m.getSourceSegmentId());
        vo.setSegmentName(m.getSegmentName());
        vo.setInspectionDate(m.getInspectionDate());
        LocalDate commission = anchor.getSegment().getCommissionDate();
        vo.setServiceYears(commission != null && m.getInspectionDate() != null
                ? NumberUtil.round(DateUtil.yearsBetween(commission, m.getInspectionDate()), 4) : null);
        vo.setOuterDiameterMm(m.getOuterDiameterMm());
        vo.setWallThicknessMm(m.getWallThicknessMm());
        vo.setMaterialGrade(m.getMaterialGrade());
        vo.setDesignPressureMpa(m.getDesignPressureMpa());
        vo.setOperatingPressureMpa(m.getOperatingPressureMpa());
        vo.setOperatingPressureText(m.getOperatingPressureText());
        vo.setPressureFactor(m.getPressureFactor());
        vo.setOperatingTemperature(m.getOperatingTemperature());
        vo.setMediumType(m.getMediumType());
        vo.setLiquidAccumulation(m.getLiquidAccumulation());
        vo.setCo2MolPct(m.getCo2MolPct());
        vo.setH2sMolPct(m.getH2sMolPct());
        vo.setO2MolPct(m.getO2MolPct());
        vo.setWaterCutPct(m.getWaterCutPct());
        vo.setChlorideMgL(m.getChlorideMgL());
        vo.setPh(m.getPh());
        vo.setInhibitorDosing(m.getInhibitorDosing());
        vo.setInhibitorConcentration(m.getInhibitorConcentration());
        vo.setPiggingCycleDays(m.getPiggingCycleDays());
        vo.setDefectCount(m.getDefectCount());
        vo.setMaxDepthPct(m.getMaxDepthPct());
        vo.setMaxDepthMm(m.getWallThicknessMm() != null && m.getMaxDepthPct() != null ? NumberUtil.round(
                PowerLawCorrosionAlgorithm.initialDepth(m.getWallThicknessMm(), m.getMaxDepthPct()), 3) : null);
        vo.setAvgErf(m.getAvgErf());
        vo.setMaxErf(m.getMaxErf());
        vo.setRemark(m.getRemark());
        vo.setIsLatest(anchor.getAnchor() != null && anchor.getAnchor().getInspectionId().equals(m.getInspectionId()));
        return vo;
    }

    private ApparentExponentVO toApparentExponent(CorrosionInspectionModel first, CorrosionInspectionModel second) {
        if (!first.getWallThicknessMm().equals(second.getWallThicknessMm())
                || !first.getInspectionDate().isBefore(second.getInspectionDate())) {
            return null;
        }
        LocalDate commission = first.getCommissionDate() != null ? first.getCommissionDate() : second.getCommissionDate();
        double t1 = DateUtil.yearsBetween(commission, first.getInspectionDate());
        double t2 = DateUtil.yearsBetween(commission, second.getInspectionDate());
        int compare = Double.compare(second.getMaxDepthPct(), first.getMaxDepthPct());
        TrendDirectionType direction = compare > 0 ? TrendDirectionType.INCREASE
                : compare < 0 ? TrendDirectionType.DECREASE : TrendDirectionType.FLAT;
        ApparentExponentVO vo = new ApparentExponentVO();
        vo.setSourceSegmentId(second.getSourceSegmentId());
        vo.setSegmentName(second.getSegmentName());
        vo.setDate1(first.getInspectionDate());
        vo.setDate2(second.getInspectionDate());
        vo.setT1Years(NumberUtil.round(t1, 3));
        vo.setT2Years(NumberUtil.round(t2, 3));
        vo.setD1Pct(first.getMaxDepthPct());
        vo.setD2Pct(second.getMaxDepthPct());
        vo.setNApp(NumberUtil.round(PowerLawCorrosionAlgorithm.apparentExponent(first.getMaxDepthPct(),
                second.getMaxDepthPct(), t1, t2), 4));
        vo.setDirection(direction.name());
        vo.setDirectionName(direction.getDisplayName());
        vo.setJudgement(direction == TrendDirectionType.DECREASE ? "需核查下降原因" : "需缺陷匹配");
        return vo;
    }

    private double baseK(SegmentAnchorDTO anchor) {
        return FixedPowerLawModel.require(anchor.getSegment().getSegmentName()).getK();
    }

    private Double median(List<Double> sorted) {
        if (sorted.isEmpty()) {
            return null;
        }
        int middle = sorted.size() / 2;
        return sorted.size() % 2 == 1 ? sorted.get(middle) : (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
    }
}
