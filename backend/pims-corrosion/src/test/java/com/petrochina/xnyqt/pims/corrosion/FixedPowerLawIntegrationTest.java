package com.petrochina.xnyqt.pims.corrosion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.DateUtil;
import com.petrochina.xnyqt.pims.corrosion.algorithm.FixedPowerLawModel;
import com.petrochina.xnyqt.pims.corrosion.controller.CorrosionController;
import com.petrochina.xnyqt.pims.corrosion.dto.PredictRequestDTO;
import com.petrochina.xnyqt.pims.corrosion.dto.SegmentAnchorDTO;
import com.petrochina.xnyqt.pims.corrosion.service.*;
import com.petrochina.xnyqt.pims.corrosion.vo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.cloud.sentinel.enabled=false", "spring.jmx.enabled=false"})
@ActiveProfiles("local")
@AutoConfigureMockMvc
class FixedPowerLawIntegrationTest {
    @Autowired ICorrosionParameterInterFace parameters;
    @Autowired ICorrosionPredictInterFace predictions;
    @Autowired ICorrosionDataInterFace data;
    @Autowired PowerLawReportService reports;
    @Autowired CorrosionController controller;
    @Autowired ObjectMapper mapper;
    @Autowired MockMvc mvc;

    @Test
    void retainsFiveSegmentsAndPredictsOnlyThreeWithExactFixedParameters() {
        List<CorrosionSegmentVO> segments = data.listSegments();
        assertEquals(5, segments.size());
        assertEquals(3, data.getOverview().getPredictableCount());
        assertEquals(3, predictions.summarizeAll(5).size());
        for (CorrosionSegmentVO segment : segments) {
            FixedPowerLawModel fixed = FixedPowerLawModel.find(segment.getSegmentName());
            assertEquals(fixed != null, segment.getPredictable());
            assertFalse(data.listInspections(segment.getSegmentId()).isEmpty());
            if (fixed == null) {
                assertTrue(segment.getNote().contains("未纳入"));
                assertThrows(BusinessException.class, () -> parameters.getParameter(segment.getSegmentId(), Double.NaN));
                PredictRequestDTO excluded = new PredictRequestDTO();
                excluded.setSegmentId(segment.getSegmentId());
                assertThrows(BusinessException.class, () -> predictions.predict(excluded));
                continue;
            }
            ModelParameterVO parameter = parameters.getParameter(segment.getSegmentId(), Double.NaN);
            assertEquals(fixed.getK(), parameter.getKValue(), 0);
            assertEquals(fixed.getN(), parameter.getN(), 0);
            assertThrows(BusinessException.class, () -> parameters.getParameter(segment.getSegmentId(), 1.0));
            PredictRequestDTO request = new PredictRequestDTO();
            request.setSegmentId(segment.getSegmentId());
            request.setTargetDate(LocalDate.of(2028, 12, 31));
            request.setCompareScenarios(true);
            PredictionVO result = predictions.predict(request);
            double years = ChronoUnit.DAYS.between(parameter.getCommissionDate(), request.getTargetDate()) / 365.2422;
            assertEquals(fixed.depth(years, parameter.getWallThicknessMm()), result.getMain().getPredDepthMm(), 0.00051);
            assertEquals(fixed.getK(), result.getMain().getKValue(), 0);
            assertTrue(result.getScenarios().isEmpty());
            assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("不能作为独立验证")));
            PredictSummaryVO summary = predictions.summarizeAll(5).stream()
                    .filter(s -> s.getSegmentId().equals(segment.getSegmentId())).findFirst().get();
            request.setTargetDate(summary.getTargetDate());
            assertEquals(summary.getPredDepthMm(), predictions.predict(request).getMain().getPredDepthMm());
            request.setTargetDate(LocalDate.of(2100, 12, 31));
            PredictionVO stoppedPrediction = predictions.predict(request);
            assertEquals(parameter.getWallThicknessMm(), stoppedPrediction.getMain().getPredDepthMm());
            List<CurvePointVO> curve = stoppedPrediction.getMain().getCurve();
            assertEquals(parameter.getWallThicknessMm(), curve.get(curve.size() - 1).getDepthMm());
            assertTrue(curve.stream().allMatch(p -> p.getDepthMm() <= parameter.getWallThicknessMm()));
            request.setScenarioN(1.0);
            assertThrows(BusinessException.class, () -> predictions.predict(request));
        }
        assertEquals(0.22517, FixedPowerLawModel.FENG_RU_A.getK(), 0);
        assertEquals(1.07087, FixedPowerLawModel.FENG_RU_A.getN(), 0);
        assertEquals(0.01697, FixedPowerLawModel.WAN_WO_B.getK(), 0);
        assertEquals(1.72263, FixedPowerLawModel.WAN_WO_B.getN(), 0);
        assertEquals(0.05313, FixedPowerLawModel.WAN_WO_C.getK(), 0);
        assertEquals(1.29786, FixedPowerLawModel.WAN_WO_C.getN(), 0);
    }

    @Test
    void parameterEndpointDefaultsToSegmentExponentAndRejectsCustomN() throws Exception {
        for (CorrosionSegmentVO segment : data.listSegments()) {
            if (!Boolean.TRUE.equals(segment.getPredictable())) { continue; }
            JsonNode response = mapper.readTree(mvc.perform(get("/corrosion/segments/" + segment.getSegmentId() + "/parameter"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
            FixedPowerLawModel fixed = FixedPowerLawModel.require(segment.getSegmentName());
            assertEquals(fixed.getK(), response.get("data").get("kValue").asDouble(), 0);
            assertEquals(fixed.getN(), response.get("data").get("n").asDouble(), 0);
        }
    }

    @Test
    void changingMeasuredDepthDoesNotReanchorFixedModel() {
        SegmentAnchorDTO anchor = parameters.evaluateAllAnchors().stream()
                .filter(SegmentAnchorDTO::isPredictable).findFirst().get();
        ICorrosionParameterInterFace source = org.mockito.Mockito.mock(ICorrosionParameterInterFace.class);
        org.mockito.Mockito.when(source.evaluateAllAnchors()).thenReturn(java.util.Collections.singletonList(anchor));
        PowerLawReportService service = new PowerLawReportService(source);
        PowerLawReportVO before = service.report().get(0);
        anchor.getInspections().forEach(i -> i.setMaxDepthPct(10.0));
        PowerLawReportVO after = service.report().get(0);
        assertEquals(before.getKValue(), after.getKValue());
        assertEquals(before.getCurve(), after.getCurve());
        assertEquals(before.getComparison().get(0).getCalculatedDepthMm(), after.getComparison().get(0).getCalculatedDepthMm());
        assertNotEquals(before.getComparison().get(0).getMeasuredDepthMm(), after.getComparison().get(0).getMeasuredDepthMm());
        assertNotEquals(before.getComparison().get(0).getErrorMm(), after.getComparison().get(0).getErrorMm());
    }

    @Test
    void reportComparesSourceMeasurementsAndStopsExactlyAtWall() throws Exception {
        List<PowerLawReportVO> result = reports.report();
        assertEquals(3, result.size());
        Map<Long, SegmentAnchorDTO> anchors = parameters.evaluateAllAnchors().stream()
                .collect(Collectors.toMap(a -> a.getSegment().getSegmentId(), a -> a));
        boolean sawStopped = false;
        for (PowerLawReportVO report : result) {
            FixedPowerLawModel fixed = FixedPowerLawModel.require(report.getSegmentName());
            double wall = report.getWallThicknessMm();
            double cutoff = Math.pow(wall / fixed.getK(), 1.0 / fixed.getN());
            assertEquals(report.getCommissionDate().plusDays(Math.round(cutoff * 365.2422)), report.getCutoffDate());
            assertFalse(report.getComparison().isEmpty());
            for (PowerLawReportVO.Comparison c : report.getComparison()) {
                double years = ChronoUnit.DAYS.between(report.getCommissionDate(), c.getDate()) / 365.2422;
                assertEquals(years, c.getTYears(), 1e-12);
                double measured = anchors.get(report.getSegmentId()).getInspections().stream()
                        .filter(i -> i.getInspectionDate().equals(c.getDate())).findFirst().get().getMaxDepthPct() * wall / 100;
                assertEquals(measured, c.getMeasuredDepthMm(), 1e-12);
                assertEquals(Math.min(wall, fixed.getK() * Math.pow(years, fixed.getN())), c.getCalculatedDepthMm(), 1e-12);
                assertEquals(c.getCalculatedDepthMm() - measured, c.getErrorMm(), 1e-12);
            }
            assertEquals(report.getCommissionDate(), report.getCurve().get(0).getDate());
            assertEquals(0, report.getCurve().get(0).getDepthMm(), 0);
            CurvePointVO last = report.getCurve().get(report.getCurve().size() - 1);
            if (!report.getCutoffDate().isAfter(LocalDate.of(2031, 12, 31))) {
                assertEquals(wall, last.getDepthMm(), 0);
                assertEquals(cutoff, last.getTYears(), 1e-12);
                assertEquals(report.getCutoffDate(), last.getDate());
                assertEquals(1, report.getCurve().stream().filter(p -> p.getDepthMm() == wall).count());
            } else {
                assertEquals(LocalDate.of(2031, 12, 31), last.getDate());
            }
            for (CurvePointVO point : report.getCurve()) {
                assertTrue(point.getDepthMm() <= wall);
                assertTrue(point.getTYears() <= cutoff);
            }
            for (int year = report.getCommissionDate().getYear(); year <= 2031; year++) {
                LocalDate end = LocalDate.of(year, 12, 31);
                if (DateUtil.yearsBetween(report.getCommissionDate(), end) < cutoff) {
                    assertTrue(report.getCurve().stream().anyMatch(p -> p.getDate().equals(end)));
                }
            }
            assertEquals(6, report.getAnnual().size());
            for (PowerLawReportVO.Annual annual : report.getAnnual()) {
                assertEquals(LocalDate.of(annual.getYear(), 12, 31), annual.getDate());
                if (annual.getDate().isAfter(report.getCutoffDate())) {
                    assertTrue(annual.isStopped());
                    assertNull(annual.getDepthMm());
                    sawStopped = true;
                } else {
                    assertFalse(annual.isStopped());
                    assertEquals(fixed.depth(DateUtil.yearsBetween(report.getCommissionDate(), annual.getDate()), wall), annual.getDepthMm(), 1e-12);
                }
            }
        }
        assertTrue(sawStopped);
        JsonNode response = mapper.readTree(mvc.perform(get("/corrosion/power-law-report"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        JsonNode row = response.get("data").get(0);
        assertTrue(row.has("kValue"));
        assertTrue(row.get("curve").get(0).has("tYears"));
        assertEquals(10, row.size());
        assertTrue(row.get("annual").get(0).has("stopped"));
    }
}
