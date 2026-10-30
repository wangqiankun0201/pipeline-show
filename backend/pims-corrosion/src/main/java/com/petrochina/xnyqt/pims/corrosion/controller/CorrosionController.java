package com.petrochina.xnyqt.pims.corrosion.controller;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.corrosion.dto.PredictRequestDTO;
import com.petrochina.xnyqt.pims.corrosion.service.PowerLawReportService;
import com.petrochina.xnyqt.pims.corrosion.vo.PowerLawReportVO;
import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionDataInterFace;
import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionParameterInterFace;
import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionPredictInterFace;
import com.petrochina.xnyqt.pims.corrosion.vo.ApparentExponentVO;
import com.petrochina.xnyqt.pims.corrosion.vo.CorrosionOverviewVO;
import com.petrochina.xnyqt.pims.corrosion.vo.CorrosionSegmentVO;
import com.petrochina.xnyqt.pims.corrosion.vo.InspectionImportResultVO;
import com.petrochina.xnyqt.pims.corrosion.vo.InspectionVO;
import com.petrochina.xnyqt.pims.corrosion.vo.ModelParameterVO;
import com.petrochina.xnyqt.pims.corrosion.vo.PredictSummaryVO;
import com.petrochina.xnyqt.pims.corrosion.vo.PredictionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Name: CorrosionController
 * @Description: 内腐蚀趋势预测接口
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Api(tags = "内腐蚀趋势预测")
@Validated
@RestController
@RequestMapping("/corrosion")
public class CorrosionController {

    //Excel 文件大小上限（字节）
    private static final long MAX_EXCEL_BYTES = 10L * 1024 * 1024;

    private final ICorrosionDataInterFace dataService;

    private final ICorrosionParameterInterFace parameterService;

    private final ICorrosionPredictInterFace predictService;

    private final PowerLawReportService reportService;

    public CorrosionController(ICorrosionDataInterFace dataService, ICorrosionParameterInterFace parameterService,
                               ICorrosionPredictInterFace predictService, PowerLawReportService reportService) {
        this.dataService = dataService;
        this.parameterService = parameterService;
        this.predictService = predictService;
        this.reportService = reportService;
    }

    @ApiOperation("固定参数幂律报告（内检拟合比较，非独立验证）")
    @GetMapping("/power-law-report")
    public ApiResult<List<PowerLawReportVO>> powerLawReport() {
        return ApiResult.success(reportService.report());
    }

    @ApiOperation("内腐蚀概览")
    @GetMapping("/overview")
    public ApiResult<CorrosionOverviewVO> getOverview() {
        return ApiResult.success(dataService.getOverview());
    }

    @ApiOperation("管段腐蚀汇总")
    @GetMapping("/segments")
    public ApiResult<List<CorrosionSegmentVO>> listSegments() {
        return ApiResult.success(dataService.listSegments());
    }

    @ApiOperation("管段内检记录")
    @GetMapping("/segments/{segmentId}/inspections")
    public ApiResult<List<InspectionVO>> listInspections(@PathVariable("segmentId") Long segmentId) {
        return ApiResult.success(dataService.listInspections(segmentId));
    }

    @ApiOperation("管段模型参数")
    @GetMapping("/segments/{segmentId}/parameter")
    public ApiResult<ModelParameterVO> getParameter(@PathVariable("segmentId") Long segmentId,
            @RequestParam(value = "n", required = false)
            @DecimalMin(value = "0.1", message = "n 须不小于0.1") @DecimalMax(value = "3.0", message = "n 须不大于3.0")
                    Double n) {
        return ApiResult.success(parameterService.getParameter(segmentId, n == null ? Double.NaN : n));
    }

    @ApiOperation("趋势预测")
    @PostMapping("/predict")
    public ApiResult<PredictionVO> predict(@Valid @RequestBody PredictRequestDTO request) {
        return ApiResult.success(predictService.predict(request));
    }

    @ApiOperation("双期记录表观指数")
    @GetMapping("/apparent-exponents")
    public ApiResult<List<ApparentExponentVO>> listApparentExponents() {
        return ApiResult.success(dataService.listApparentExponents());
    }

    @ApiOperation("导入内腐蚀数据（Excel 全量替换）")
    @PostMapping("/inspections/import")
    public ApiResult<InspectionImportResultVO> importInspections(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (file.isEmpty() || file.getSize() > MAX_EXCEL_BYTES) {
            throw new BusinessException("文件为空或超过 10MB");
        }
        if (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls")) {
            throw new BusinessException("仅支持 .xlsx / .xls 文件");
        }
        try (InputStream input = file.getInputStream()) {
            return ApiResult.success(dataService.importInspections(input, filename));
        } catch (IOException e) {
            throw new BusinessException("文件读取失败");
        }
    }

    @ApiOperation("内部接口：全部管段预测摘要（失效风险服务调用）")
    @GetMapping("/inner/predict-summary")
    public ApiResult<List<PredictSummaryVO>> summarizeAll(
            @RequestParam(value = "yearsAfterIli", defaultValue = "5")
            @DecimalMin(value = "0.01", message = "年数须大于0") @DecimalMax(value = "50", message = "年数不超过50")
                    Double yearsAfterIli) {
        return ApiResult.success(predictService.summarizeAll(yearsAfterIli));
    }
}
