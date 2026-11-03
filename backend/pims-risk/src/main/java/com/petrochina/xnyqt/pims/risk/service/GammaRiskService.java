package com.petrochina.xnyqt.pims.risk.service;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.risk.algorithm.GammaRiskAlgorithm;
import com.petrochina.xnyqt.pims.risk.dto.GammaPredictRequestDTO;
import com.petrochina.xnyqt.pims.risk.feign.CorrosionFeignClient;
import com.petrochina.xnyqt.pims.risk.feign.dto.CorrosionSegmentDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.InspectionDTO;
import com.petrochina.xnyqt.pims.risk.vo.GammaPredictionVO;
import com.petrochina.xnyqt.pims.risk.vo.GammaSegmentVO;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GammaRiskService {
    private static final String VERSION = "CONDITIONAL_GAMMA_V1";
    private static final Map<String, double[]> PARAMETERS = new LinkedHashMap<>();
    static {
        PARAMETERS.put("峰汝线A段", new double[]{0.22517, 1.07087});
        PARAMETERS.put("万卧线B段", new double[]{0.01697, 1.72263});
        PARAMETERS.put("万卧线C段", new double[]{0.05313, 1.29786});
    }
    private final CorrosionFeignClient corrosion;
    private final GammaRiskAlgorithm algorithm;

    public GammaRiskService(CorrosionFeignClient corrosion, GammaRiskAlgorithm algorithm) {
        this.corrosion = corrosion;
        this.algorithm = algorithm;
    }

    public List<GammaSegmentVO> listSegments() {
        List<CorrosionSegmentDTO> all = loadSegments();
        List<GammaSegmentVO> result = new ArrayList<>();
        for (String name : PARAMETERS.keySet()) {
            CorrosionSegmentDTO segment = all.stream().filter(s -> name.equals(s.getSegmentName()))
                    .findFirst().orElseThrow(() -> new BusinessException("Gamma模型缺少管段数据：" + name));
            List<InspectionDTO> inspections = validInspections(segment);
            InspectionDTO latest = inspections.get(inspections.size() - 1);
            GammaSegmentVO vo = new GammaSegmentVO();
            vo.setSegmentId(segment.getSegmentId());
            vo.setSegmentName(segment.getSegmentName());
            vo.setPipelineName(segment.getPipelineName());
            vo.setCommissionDate(segment.getCommissionDate());
            vo.setWallThicknessMm(segment.getWallThicknessMm());
            vo.setInspectionDate(latest.getInspectionDate());
            vo.setInspectionDepthMm(depth(latest));
            vo.setKValue(PARAMETERS.get(name)[0]);
            vo.setN(PARAMETERS.get(name)[1]);
            vo.setStatus(depth(latest) >= segment.getWallThicknessMm() ? "ALREADY_PENETRATED" : "OK");
            result.add(vo);
        }
        return result;
    }

    public GammaPredictionVO predict(GammaPredictRequestDTO request) {
        if (request == null || request.getSegmentId() == null) { throw new BusinessException("请选择管段"); }
        double theta = request.getTheta() == null ? 0.2 : request.getTheta();
        int count = request.getSampleCount() == null ? 100000 : request.getSampleCount();
        long seed = request.getSeed() == null ? 20261029L : request.getSeed();
        if (!Double.isFinite(theta) || theta < 0.001 || theta > 5) {
            throw new BusinessException("theta须在0.001至5之间");
        }
        if (count < 1000 || count > 200000) { throw new BusinessException("sampleCount须在1000至200000之间"); }
        CorrosionSegmentDTO segment = loadSegments().stream()
                .filter(s -> request.getSegmentId().equals(s.getSegmentId())).findFirst()
                .orElseThrow(() -> new BusinessException("管段不存在"));
        double[] parameters = PARAMETERS.get(segment.getSegmentName());
        if (parameters == null) { throw new BusinessException("Gamma模型仅支持峰汝线A段、万卧线B段及万卧线C段"); }
        List<InspectionDTO> inspections = validInspections(segment);
        InspectionDTO latest = inspections.get(inspections.size() - 1);
        GammaPredictionVO vo = new GammaPredictionVO();
        vo.setSegmentId(segment.getSegmentId());
        vo.setSegmentName(segment.getSegmentName());
        vo.setModelVersion(VERSION);
        vo.setCommissionDate(segment.getCommissionDate());
        vo.setInspectionDate(latest.getInspectionDate());
        vo.setInspectionDepthMm(depth(latest));
        vo.setWallThicknessMm(segment.getWallThicknessMm());
        vo.setKValue(parameters[0]);
        vo.setN(parameters[1]);
        vo.setTheta(theta);
        vo.setSampleCount(count);
        vo.setSeed(seed);
        for (InspectionDTO inspection : inspections) {
            GammaPredictionVO.History history = new GammaPredictionVO.History();
            history.setDate(inspection.getInspectionDate());
            history.setMeasuredDepthMm(depth(inspection));
            double calculated = parameters[0] * Math.pow(GammaRiskAlgorithm.years(
                    segment.getCommissionDate(), inspection.getInspectionDate()), parameters[1]);
            GammaRiskAlgorithm.finite(calculated);
            history.setCalculatedDepthMm(calculated);
            vo.getHistory().add(history);
        }
        if (vo.getInspectionDepthMm() >= vo.getWallThicknessMm()) {
            vo.setStatus("ALREADY_PENETRATED");
            vo.getWarnings().add("最新有效内检深度已达到或超过壁厚，已穿透，不作正常未穿透外推");
            return vo;
        }
        vo.setStatus("OK");
        if (latest.getInspectionDate().isAfter(LocalDate.of(2026, 12, 31))) {
            throw new BusinessException("最新有效内检日期晚于2026年末，无法生成完整2026至2031条件预测");
        }
        double ti = GammaRiskAlgorithm.years(vo.getCommissionDate(), vo.getInspectionDate());
        for (int year = 2026; year <= 2031; year++) {
            GammaPredictionVO.Annual row = new GammaPredictionVO.Annual();
            row.setYear(year);
            row.setDate(LocalDate.of(year, 12, 31));
            row.setTYears(GammaRiskAlgorithm.years(vo.getCommissionDate(), row.getDate()));
            double shape = algorithm.shape(row.getTYears(), ti, parameters[0], parameters[1], theta);
            row.setShape(shape);
            double mean = vo.getInspectionDepthMm() + shape * theta;
            GammaRiskAlgorithm.finite(mean);
            row.setMeanDepthMm(mean);
            row.setCappedMeanDepthMm(Math.min(vo.getWallThicknessMm(), mean));
            row.setRemainingWallMm(Math.max(0, vo.getWallThicknessMm() - mean));
            row.setProbability(algorithm.probability(shape, vo.getWallThicknessMm() - vo.getInspectionDepthMm(), theta));
            vo.getAnnual().add(row);
        }
        if (vo.getAnnual().stream().anyMatch(r -> r.getMeanDepthMm() >= vo.getWallThicknessMm())) {
            vo.getWarnings().add("均值深度达到壁厚后，穿透概率仍继续计算至2031年。");
        }
        algorithm.simulate(vo);
        return vo;
    }

    private List<CorrosionSegmentDTO> loadSegments() {
        List<CorrosionSegmentDTO> all = unwrap(corrosion.listSegments(), "管段");
        return all.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<InspectionDTO> validInspections(CorrosionSegmentDTO segment) {
        if (segment.getSegmentId() == null || segment.getCommissionDate() == null
                || segment.getCommissionDate().isAfter(LocalDate.now())
                || !positive(segment.getWallThicknessMm())) {
            throw new BusinessException(segment.getSegmentName() + "缺少有效投运日期或壁厚");
        }
        List<InspectionDTO> valid = unwrap(corrosion.listInspections(segment.getSegmentId()), "内检").stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getInspectionDate() != null
                        && !i.getInspectionDate().isBefore(segment.getCommissionDate())
                        && !i.getInspectionDate().isAfter(LocalDate.now()))
                .filter(i -> Double.isFinite(depth(i)) && depth(i) >= 0)
                .sorted(Comparator.comparing(InspectionDTO::getInspectionDate)
                        .thenComparingDouble(GammaRiskService::depth))
                .collect(Collectors.toList());
        if (valid.isEmpty()) { throw new BusinessException(segment.getSegmentName() + "无有效内检深度数据"); }
        return valid;
    }

    private static boolean positive(Double value) {
        return value != null && Double.isFinite(value) && value > 0;
    }

    private static double depth(InspectionDTO inspection) {
        if (inspection.getMaxDepthPct() != null) {
            if (!positive(inspection.getWallThicknessMm()) || !Double.isFinite(inspection.getMaxDepthPct())
                    || inspection.getMaxDepthPct() < 0) { return Double.NaN; }
            return inspection.getWallThicknessMm() * inspection.getMaxDepthPct() / 100.0;
        }
        return inspection.getMaxDepthMm() == null ? Double.NaN : inspection.getMaxDepthMm();
    }

    private static <T> List<T> unwrap(ApiResult<List<T>> response, String dataName) {
        if (response == null || !response.isSuccess() || response.getData() == null || response.getData().isEmpty()) {
            throw new BusinessException("腐蚀服务未提供有效" + dataName + "数据，请检查服务连接与数据导入");
        }
        return response.getData();
    }
}
