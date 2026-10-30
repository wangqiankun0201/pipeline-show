package com.petrochina.xnyqt.pims.corrosion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.NumberUtil;
import com.petrochina.xnyqt.pims.common.util.DateUtil;
import com.petrochina.xnyqt.pims.corrosion.algorithm.PowerLawCorrosionAlgorithm;
import com.petrochina.xnyqt.pims.corrosion.algorithm.FixedPowerLawModel;
import com.petrochina.xnyqt.pims.corrosion.config.CorrosionModelProperties;
import com.petrochina.xnyqt.pims.corrosion.dao.CorrosionInspectionDAO;
import com.petrochina.xnyqt.pims.corrosion.dao.CorrosionModelParamDAO;
import com.petrochina.xnyqt.pims.corrosion.dao.CorrosionSegmentDAO;
import com.petrochina.xnyqt.pims.corrosion.dto.SegmentAnchorDTO;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionInspectionModel;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionModelParamModel;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionSegmentModel;
import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionParameterInterFace;
import com.petrochina.xnyqt.pims.corrosion.type.GradeType;
import com.petrochina.xnyqt.pims.corrosion.type.TimeOriginType;
import com.petrochina.xnyqt.pims.corrosion.vo.ModelParameterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @Name: CorrosionParameterImpl
 * @Description: 幂律模型参数业务实现，口径遵循技术说明 2.2、4.1、6.2
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Service
public class CorrosionParameterImpl implements ICorrosionParameterInterFace {

    //检测记录排序：内检日期升序（无日期在后），同日按行号
    public static final Comparator<CorrosionInspectionModel> INSPECTION_ORDER = Comparator
            .comparing(CorrosionInspectionModel::getInspectionDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(CorrosionInspectionModel::getRowNo, Comparator.nullsLast(Comparator.naturalOrder()));

    private final CorrosionSegmentDAO segmentDAO;

    private final CorrosionInspectionDAO inspectionDAO;

    private final CorrosionModelParamDAO paramDAO;

    private final CorrosionModelProperties properties;

    public CorrosionParameterImpl(CorrosionSegmentDAO segmentDAO, CorrosionInspectionDAO inspectionDAO,
                                  CorrosionModelParamDAO paramDAO, CorrosionModelProperties properties) {
        this.segmentDAO = segmentDAO;
        this.inspectionDAO = inspectionDAO;
        this.paramDAO = paramDAO;
        this.properties = properties;
    }

    @Override
    public SegmentAnchorDTO evaluateAnchor(Long segmentId) {
        CorrosionSegmentModel segment = segmentDAO.selectById(segmentId);
        if (segment == null) {
            throw new BusinessException("管段不存在：" + segmentId);
        }
        List<CorrosionInspectionModel> inspections = inspectionDAO.selectList(
                new LambdaQueryWrapper<CorrosionInspectionModel>().eq(CorrosionInspectionModel::getSegmentId, segmentId));
        return evaluate(segment, inspections);
    }

    @Override
    public List<SegmentAnchorDTO> evaluateAllAnchors() {
        Map<Long, List<CorrosionInspectionModel>> grouped = inspectionDAO.selectList(null).stream()
                .collect(Collectors.groupingBy(CorrosionInspectionModel::getSegmentId));
        List<CorrosionSegmentModel> segments = segmentDAO.selectList(new LambdaQueryWrapper<CorrosionSegmentModel>()
                .orderByAsc(CorrosionSegmentModel::getMine, CorrosionSegmentModel::getPipelineName,
                        CorrosionSegmentModel::getSegmentId));
        return segments.stream()
                .map(segment -> evaluate(segment, grouped.getOrDefault(segment.getSegmentId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    public ModelParameterVO buildParameter(SegmentAnchorDTO anchor, double n) {
        if (!anchor.isPredictable()) {
            throw new BusinessException(anchor.getSegment().getSegmentName() + " 不满足计算条件："
                    + String.join("；", anchor.getNotes()));
        }
        CorrosionInspectionModel record = anchor.getAnchor();
        CorrosionSegmentModel segment = anchor.getSegment();
        FixedPowerLawModel fixed = FixedPowerLawModel.require(segment.getSegmentName());
        fixed.checkN(n);
        double k = fixed.getK();
        ModelParameterVO vo = new ModelParameterVO();
        vo.setSegmentId(segment.getSegmentId());
        vo.setSegmentName(segment.getSegmentName());
        vo.setModelName(properties.getModelName());
        vo.setModelVersion(properties.getModelVersion());
        vo.setN(n);
        vo.setKValue(k);
        vo.setKUnit("mm/年^" + NumberUtil.round(n, 2));
        vo.setD0Mm(NumberUtil.round(anchor.getD0Mm(), 3));
        vo.setTYears(NumberUtil.round(anchor.getAnchorYears(), 4));
        vo.setMaxDepthPct(record.getMaxDepthPct());
        vo.setWallThicknessMm(record.getWallThicknessMm());
        vo.setAnchorDate(record.getInspectionDate());
        vo.setCommissionDate(segment.getCommissionDate());
        vo.setTimeOrigin(TimeOriginType.COMMISSION_DATE.name());
        vo.setTimeOriginName(TimeOriginType.COMMISSION_DATE.getDisplayName());
        vo.setGrade(anchor.getGrade().name());
        vo.setGradeName(anchor.getGrade().getDisplayName());
        vo.setParameterSource("用户指定固定参数；d=k*t^n，t=投运日起天数/365.2422；内检仅作拟合比较，不构成独立验证");
        return vo;
    }

    @Override
    public ModelParameterVO getParameter(Long segmentId, double n) {
        SegmentAnchorDTO anchor = evaluateAnchor(segmentId);
        FixedPowerLawModel fixed = FixedPowerLawModel.require(anchor.getSegment().getSegmentName());
        if (!Double.isNaN(n)) { fixed.checkN(n); }
        return buildParameter(anchor, fixed.getN());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshParameterLibrary() {
        paramDAO.delete(new LambdaQueryWrapper<CorrosionModelParamModel>()
                .eq(CorrosionModelParamModel::getModelVersion, properties.getModelVersion()));
        int count = 0;
        for (SegmentAnchorDTO anchor : evaluateAllAnchors()) {
            //仅可计算管段写入参数库
            if (!anchor.isPredictable()) {
                continue;
            }
            paramDAO.insert(toParamModel(anchor, buildParameter(anchor, FixedPowerLawModel.require(anchor.getSegment().getSegmentName()).getN())));
            count++;
        }
        log.info("固定幂律参数库已刷新：模型 {}，参数 {} 条", properties.getModelVersion(), count);
        return count;
    }

    /**
     * @Name: evaluate
     * @Description: 评估管段可计算性并确定锚点（最近一次内检日期、深度、壁厚齐全的记录）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: CorrosionSegmentModel segment 管段, List inspections 检测记录
     * @Return: SegmentAnchorDTO 评估结果
     */
    private SegmentAnchorDTO evaluate(CorrosionSegmentModel segment, List<CorrosionInspectionModel> inspections) {
        SegmentAnchorDTO dto = new SegmentAnchorDTO();
        dto.setSegment(segment);
        List<CorrosionInspectionModel> sorted = new ArrayList<>(inspections);
        sorted.sort(INSPECTION_ORDER);
        dto.setInspections(sorted);
        CorrosionInspectionModel anchor = sorted.stream().filter(CorrosionParameterImpl::isValidInspection)
                .max(Comparator.comparing(CorrosionInspectionModel::getInspectionDate)).orElse(null);
        String reason = FixedPowerLawModel.find(segment.getSegmentName()) == null
                ? "未纳入固定参数预测范围，仅保留基础资料与内检信息" : checkComputable(segment, sorted, anchor);
        dto.setAnchor(anchor);
        if (anchor != null && segment.getCommissionDate() != null) {
            dto.setAnchorYears(DateUtil.yearsBetween(segment.getCommissionDate(), anchor.getInspectionDate()));
            dto.setD0Mm(PowerLawCorrosionAlgorithm.initialDepth(anchor.getWallThicknessMm(), anchor.getMaxDepthPct()));
        }
        //多个源管段ID归并时提示核实
        if (segment.getSourceSegmentIds() != null && segment.getSourceSegmentIds().contains(",")) {
            dto.getNotes().add("同名管段归并了源管段ID " + segment.getSourceSegmentIds().replace(",", "、")
                    + "，跨ID疑似复检，待核实是否为同一实体管段");
        }
        if (reason != null) {
            dto.getNotes().add(0, reason);
            return dto;
        }
        double years = DateUtil.yearsBetween(segment.getCommissionDate(), anchor.getInspectionDate());
        dto.setAnchor(anchor);
        dto.setAnchorYears(years);
        dto.setD0Mm(PowerLawCorrosionAlgorithm.initialDepth(anchor.getWallThicknessMm(), anchor.getMaxDepthPct()));
        dto.setPredictable(true);
        //保留既有数据等级字段；固定参数的内检比较不是独立验证
        dto.setGrade(GradeType.C);
        if (years < properties.getShortServiceYears()) {
            dto.getNotes().add(String.format("名义作用时间仅 %.2f 年，应核实时间原点和检测位置；固定参数不重新拟合", years));
        }
        return dto;
    }

    private String checkComputable(CorrosionSegmentModel segment, List<CorrosionInspectionModel> inspections,
                                   CorrosionInspectionModel anchor) {
        Set<Double> walls = inspections.stream().map(CorrosionInspectionModel::getWallThicknessMm)
                .filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
        String reason = null;
        if (anchor == null) {
            reason = "无有效内检记录（缺少内检日期、最大腐蚀深度或壁厚）";
        } else if (segment.getCommissionDate() == null) {
            reason = "缺少投运日期，无法确定时间原点";
        } else if (!segment.getCommissionDate().isBefore(anchor.getInspectionDate())) {
            reason = "投运日期不早于内检日期，时间原点异常";
        } else if (walls.size() > 1) {
            reason = "同一管段存在多个壁厚值（" + walls + "），按规则暂不自动计算参数";
        }
        return reason;
    }

    private static boolean isValidInspection(CorrosionInspectionModel inspection) {
        return inspection.getInspectionDate() != null && inspection.getWallThicknessMm() != null
                && inspection.getWallThicknessMm() > 0 && inspection.getMaxDepthPct() != null
                && inspection.getMaxDepthPct() > 0;
    }

    private CorrosionModelParamModel toParamModel(SegmentAnchorDTO anchor, ModelParameterVO parameter) {
        CorrosionModelParamModel model = new CorrosionModelParamModel();
        model.setSegmentId(parameter.getSegmentId());
        model.setModelName(parameter.getModelName());
        model.setModelVersion(parameter.getModelVersion());
        model.setExponentN(parameter.getN());
        model.setCoefficientK(parameter.getKValue());
        model.setD0Mm(anchor.getD0Mm());
        model.setAnchorYears(anchor.getAnchorYears());
        model.setAnchorDate(parameter.getAnchorDate());
        model.setCommissionDate(parameter.getCommissionDate());
        model.setWallThicknessMm(parameter.getWallThicknessMm());
        model.setMaxDepthPct(parameter.getMaxDepthPct());
        model.setAnchorSourceSegmentId(anchor.getAnchor().getSourceSegmentId());
        model.setTimeOrigin(parameter.getTimeOrigin());
        model.setGrade(parameter.getGrade());
        model.setParameterSource(parameter.getParameterSource());
        model.setCalcTime(LocalDateTime.now());
        return model;
    }
}
