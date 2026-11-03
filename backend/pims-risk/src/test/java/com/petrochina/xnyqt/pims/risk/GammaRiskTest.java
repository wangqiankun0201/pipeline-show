package com.petrochina.xnyqt.pims.risk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.risk.algorithm.GammaRiskAlgorithm;
import com.petrochina.xnyqt.pims.risk.dto.GammaPredictRequestDTO;
import com.petrochina.xnyqt.pims.risk.feign.CorrosionFeignClient;
import com.petrochina.xnyqt.pims.risk.feign.dto.CorrosionSegmentDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.InspectionDTO;
import com.petrochina.xnyqt.pims.risk.service.GammaRiskService;
import com.petrochina.xnyqt.pims.risk.vo.GammaPredictionVO;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GammaRiskTest {
    private final GammaRiskAlgorithm algorithm = new GammaRiskAlgorithm();

    @Test void knownExponentialAndInitialCondition() {
        assertEquals(Math.exp(-3), algorithm.probability(1, 0.6, 0.2), 1e-15);
        assertEquals(0, algorithm.shape(10, 10, .22517, 1.07087, .2));
        assertEquals(0, algorithm.probability(0, 1, .2));
        assertEquals(1, algorithm.probability(0, 0, .2));
    }

    @Test void extremeTailsAndInvalidInputs() {
        assertEquals(Math.exp(-100), algorithm.probability(1, 20, .2), 1e-55);
        assertTrue(algorithm.probability(1, 20, .2) > 0);
        assertEquals(0, algorithm.probability(1, 200, .2));
        assertEquals(1, algorithm.probability(1000, .01, .2), 1e-14);
        assertThrows(BusinessException.class, () -> algorithm.probability(Double.NaN, 1, .2));
        assertThrows(BusinessException.class, () -> algorithm.shape(9, 10, .2, 1, .2));
    }

    @Test void analyticProbabilityMonotone() {
        double previous = 0;
        for (int i = 0; i < 100; i++) {
            double p = algorithm.probability(i / 5.0, 2, .2);
            assertTrue(p >= previous);
            previous = p;
        }
    }

    @Test void simulationRepeatableSharedPrefixesMonotoneAndAccurate() {
        GammaPredictionVO full = simulation(100000, 2);
        GammaPredictionVO repeat = simulation(100000, 2);
        GammaPredictionVO half = simulation(50000, 2);
        assertEquals(full.getAnnual(), repeat.getAnnual());
        double previous = 0;
        for (int j = 0; j < 6; j++) {
            GammaPredictionVO.Annual row = full.getAnnual().get(j);
            assertTrue(row.getMonteCarloProbability() >= previous);
            previous = row.getMonteCarloProbability();
            double p = algorithm.probability(row.getShape(), 2, .2);
            assertEquals(p, row.getMonteCarloProbability(), 6 * Math.sqrt(p * (1-p) / 100000) + .0001);
            assertEquals(half.getAnnual().get(j).getMonteCarloProbability(),
                    full.getConvergence().get(j * 2).getProbability());
            assertEquals(row.getMonteCarloProbability(), full.getConvergence().get(j * 2 + 1).getProbability());
        }
    }

    @Test void zeroFailuresHaveExactUpperBoundAndWarning() {
        GammaPredictionVO result = simulation(1000, 1000);
        for (GammaPredictionVO.Annual row : result.getAnnual()) {
            assertEquals(0, row.getFailedSamples());
            assertEquals(1-Math.pow(.05, 1.0 / 1000), row.getZeroFailureUpper95(), 1e-15);
            assertTrue(row.getConfidenceHigh() > 0);
        }
        assertFalse(result.getWarnings().isEmpty());
    }

    private GammaPredictionVO simulation(int count, double remaining) {
        GammaPredictionVO vo = new GammaPredictionVO();
        vo.setTheta(.2); vo.setSeed(20261029); vo.setSampleCount(count);
        vo.setWallThicknessMm(remaining + 2); vo.setInspectionDepthMm(2);
        for (int j = 0; j < 6; j++) {
            GammaPredictionVO.Annual row = new GammaPredictionVO.Annual();
            row.setYear(2026 + j); row.setShape(2 + 3 * j);
            vo.getAnnual().add(row);
        }
        algorithm.simulate(vo);
        return vo;
    }

    private CorrosionSegmentDTO segment() {
        CorrosionSegmentDTO s = new CorrosionSegmentDTO();
        s.setSegmentId(4L); s.setSegmentName("峰汝线A段"); s.setPipelineName("峰汝线");
        s.setCommissionDate(LocalDate.of(1990,10,1)); s.setWallThicknessMm(11.0);
        return s;
    }
    private InspectionDTO inspection(LocalDate date, double pct) {
        InspectionDTO i = new InspectionDTO(); i.setInspectionDate(date);
        i.setWallThicknessMm(11.0); i.setMaxDepthPct(pct); i.setMaxDepthMm(0.123);
        return i;
    }
    private GammaPredictRequestDTO request() {
        GammaPredictRequestDTO r = new GammaPredictRequestDTO(); r.setSegmentId(4L); r.setSampleCount(1000); return r;
    }
    private GammaRiskService service(CorrosionSegmentDTO s, InspectionDTO... records) {
        CorrosionFeignClient client = mock(CorrosionFeignClient.class);
        when(client.listSegments()).thenReturn(ApiResult.success(Collections.singletonList(s)));
        when(client.listInspections(4L)).thenReturn(ApiResult.success(Arrays.asList(records)));
        return new GammaRiskService(client, algorithm);
    }

    @Test void latestValidExactDepthRealDatesAndUncappedFuture() {
        InspectionDTO old = inspection(LocalDate.of(2020,1,1), 50);
        InspectionDTO latest = inspection(LocalDate.of(2025,1,1), 99.12345);
        InspectionDTO invalid = inspection(LocalDate.of(2026,1,1), Double.NaN);
        GammaPredictionVO vo = service(segment(), latest, invalid, old).predict(request());
        assertEquals(11 * 99.12345 / 100, vo.getInspectionDepthMm(), 1e-14);
        assertEquals(latest.getInspectionDate(), vo.getInspectionDate());
        assertEquals(6, vo.getAnnual().size());
        GammaPredictionVO.Annual last = vo.getAnnual().get(5);
        assertEquals(LocalDate.of(2031,12,31), last.getDate());
        assertEquals(GammaRiskAlgorithm.years(segment().getCommissionDate(), last.getDate()), last.getTYears());
        assertTrue(last.getMeanDepthMm() > 11); assertEquals(11, last.getCappedMeanDepthMm());
        assertEquals(0, last.getRemainingWallMm());
        assertTrue(last.getProbability() > vo.getAnnual().get(0).getProbability());
        assertEquals(2, vo.getHistory().size());
    }

    @Test void alreadyPenetratedHasExplicitStatusWithoutNormalForecast() {
        GammaPredictionVO vo = service(segment(), inspection(LocalDate.of(2025,1,1), 101)).predict(request());
        assertEquals("ALREADY_PENETRATED", vo.getStatus());
        assertTrue(vo.getAnnual().isEmpty()); assertTrue(vo.getConvergence().isEmpty());
        assertFalse(vo.getWarnings().isEmpty());
    }

    @Test void rejectsMissingAndNonFiniteDataAndUnsupportedSegments() {
        CorrosionSegmentDTO s = segment();
        assertThrows(BusinessException.class, () -> service(s).predict(request()));
        assertThrows(BusinessException.class, () -> service(s, inspection(LocalDate.of(2025,1,1), -1)).predict(request()));
        GammaPredictRequestDTO r = request(); r.setTheta(Double.NaN);
        assertThrows(BusinessException.class, () -> service(s).predict(r));
        r.setTheta(.2); r.setSampleCount(999);
        assertThrows(BusinessException.class, () -> service(s).predict(r));
        s.setSegmentName("其他管段");
        assertThrows(BusinessException.class, () -> service(s).predict(request()));
    }

    @Test void exactJsonContractPreservesCamelCase() {
        GammaPredictionVO vo = service(segment(), inspection(LocalDate.of(2025,1,1), 90)).predict(request());
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode json = mapper.valueToTree(vo);
        assertTrue(json.has("kValue")); assertFalse(json.has("kvalue"));
        assertTrue(json.get("annual").get(0).has("tYears"));
        assertEquals(20261029, json.get("seed").asLong());
        assertEquals(.2, json.get("theta").asDouble());
    }

    @Test void actualA2ReferenceMatchesIndependentScipy() {
        GammaPredictionVO vo = service(segment(), inspection(LocalDate.of(2023,3,14), 85)).predict(request());
        assertEquals(5.886567940912033, vo.getAnnual().get(0).getShape(), 1e-12);
        assertEquals(0.15826924348482557, vo.getAnnual().get(0).getProbability(), 1e-13);
        assertEquals(2031, vo.getAnnual().get(5).getYear());
    }

    @Test void threeSelectionRecordsUseFixedParameters() {
        CorrosionFeignClient client = mock(CorrosionFeignClient.class);
        CorrosionSegmentDTO a = segment();
        CorrosionSegmentDTO b = segment(); b.setSegmentId(15L); b.setSegmentName("万卧线B段");
        CorrosionSegmentDTO c = segment(); c.setSegmentId(16L); c.setSegmentName("万卧线C段");
        when(client.listSegments()).thenReturn(ApiResult.success(Arrays.asList(c,b,a)));
        for (long id : new long[]{4,15,16}) {
            when(client.listInspections(id)).thenReturn(ApiResult.success(Collections.singletonList(
                    inspection(LocalDate.of(2023,3,14), 85))));
        }
        com.petrochina.xnyqt.pims.risk.vo.GammaSegmentVO[] choices = new GammaRiskService(client,algorithm)
                .listSegments().toArray(new com.petrochina.xnyqt.pims.risk.vo.GammaSegmentVO[0]);
        assertEquals(3, choices.length);
        assertEquals(.22517, choices[0].getKValue()); assertEquals(1.07087, choices[0].getN());
        assertEquals(.01697, choices[1].getKValue()); assertEquals(1.72263, choices[1].getN());
        assertEquals(.05313, choices[2].getKValue()); assertEquals(1.29786, choices[2].getN());
    }

    @Test void inspectionAtYearEndHasZeroShapeAndProbability() {
        //过去的2026年末尚未到达时，使用更早年份的日期验证算法初值；服务拒绝未来内检。
        double t = GammaRiskAlgorithm.years(LocalDate.of(1990,10,1), LocalDate.of(2026,12,31));
        double shape = algorithm.shape(t,t,.22517,1.07087,.2);
        assertEquals(0, shape); assertEquals(0, algorithm.probability(shape,1,.2));
    }
}
