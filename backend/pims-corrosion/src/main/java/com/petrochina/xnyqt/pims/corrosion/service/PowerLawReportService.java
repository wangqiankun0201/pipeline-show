package com.petrochina.xnyqt.pims.corrosion.service;

import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.DateUtil;
import com.petrochina.xnyqt.pims.corrosion.algorithm.FixedPowerLawModel;
import com.petrochina.xnyqt.pims.corrosion.algorithm.PowerLawCorrosionAlgorithm;
import com.petrochina.xnyqt.pims.corrosion.dto.SegmentAnchorDTO;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionInspectionModel;
import com.petrochina.xnyqt.pims.corrosion.vo.CurvePointVO;
import com.petrochina.xnyqt.pims.corrosion.vo.PowerLawReportVO;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** 固定参数报告；源内检只用于拟合比较，不能据此声称独立验证。 */
@Service
public class PowerLawReportService {
    private static final LocalDate END = LocalDate.of(2031, 12, 31);
    private final ICorrosionParameterInterFace parameterService;

    public PowerLawReportService(ICorrosionParameterInterFace parameterService) {
        this.parameterService = parameterService;
    }

    public List<PowerLawReportVO> report() {
        return parameterService.evaluateAllAnchors().stream().filter(SegmentAnchorDTO::isPredictable)
                .map(this::build).collect(Collectors.toList());
    }

    private PowerLawReportVO build(SegmentAnchorDTO anchor) {
        FixedPowerLawModel model = FixedPowerLawModel.require(anchor.getSegment().getSegmentName());
        LocalDate commission = anchor.getSegment().getCommissionDate();
        double wall = anchor.getAnchor().getWallThicknessMm();
        double cutoff = model.cutoffYears(wall);
        PowerLawReportVO vo = new PowerLawReportVO();
        vo.setSegmentId(anchor.getSegment().getSegmentId());
        vo.setSegmentName(anchor.getSegment().getSegmentName());
        vo.setCommissionDate(commission);
        vo.setWallThicknessMm(wall);
        vo.setKValue(model.getK());
        vo.setN(model.getN());
        vo.setCutoffDate(model.cutoffDate(commission, wall));
        TreeSet<LocalDate> dates = new TreeSet<>();
        //半年采样，同时补齐每个自然年末及全部内检日期。
        for (LocalDate date = commission; !date.isAfter(END); date = date.plusMonths(6)) {
            dates.add(date);
        }
        dates.add(END);
        for (int year = commission.getYear(); year <= 2031; year++) {
            dates.add(LocalDate.of(year, 12, 31));
        }
        for (CorrosionInspectionModel inspection : anchor.getInspections()) {
            LocalDate date = inspection.getInspectionDate();
            if (date == null || date.isBefore(commission)) { continue; }
            if (!date.isAfter(END)) { dates.add(date); }
            if (inspection.getMaxDepthPct() == null || inspection.getWallThicknessMm() == null) { continue; }
            double measured = PowerLawCorrosionAlgorithm.initialDepth(inspection.getWallThicknessMm(),
                    inspection.getMaxDepthPct());
            //不裁剪或伪造源实测值；异常记录拒绝报告，避免输出超过壁厚的深度。
            if (measured < 0 || measured > wall) {
                throw new BusinessException(vo.getSegmentName() + " 内检实测深度超出壁厚，需核验源数据");
            }
            double years = DateUtil.yearsBetween(commission, date);
            double calculated = model.depth(years, wall);
            PowerLawReportVO.Comparison comparison = new PowerLawReportVO.Comparison();
            comparison.setDate(date);
            comparison.setTYears(years);
            comparison.setMeasuredDepthMm(measured);
            comparison.setCalculatedDepthMm(calculated);
            comparison.setErrorMm(calculated - measured);
            vo.getComparison().add(comparison);
        }
        for (LocalDate date : dates) {
            double years = DateUtil.yearsBetween(commission, date);
            if (years >= cutoff) { break; }
            vo.getCurve().add(point(date, years, model.depth(years, wall)));
        }
        if (cutoff <= DateUtil.yearsBetween(commission, END)) {
            //精确达到壁厚的模型时间，日期按最近自然日显示；此后不生成平台线。
            vo.getCurve().add(point(vo.getCutoffDate(), cutoff, wall));
        }
        for (int year = 2026; year <= 2031; year++) {
            LocalDate date = LocalDate.of(year, 12, 31);
            double years = DateUtil.yearsBetween(commission, date);
            boolean stopped = date.isAfter(vo.getCutoffDate()) || years > cutoff;
            PowerLawReportVO.Annual annual = new PowerLawReportVO.Annual();
            annual.setYear(year);
            annual.setDate(date);
            annual.setStopped(stopped);
            annual.setDepthMm(stopped ? null : model.depth(years, wall));
            vo.getAnnual().add(annual);
        }
        return vo;
    }

    private CurvePointVO point(LocalDate date, double years, double depth) {
        CurvePointVO point = new CurvePointVO();
        point.setDate(date);
        point.setTYears(years);
        point.setDepthMm(depth);
        return point;
    }
}
