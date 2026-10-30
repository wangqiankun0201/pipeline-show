package com.petrochina.xnyqt.pims.risk.controller;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.risk.dto.AssessRequestDTO;
import com.petrochina.xnyqt.pims.risk.service.IRiskAssessInterFace;
import com.petrochina.xnyqt.pims.risk.vo.AssessmentVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskFactorGroupVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskInputsVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskMatrixVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskSegmentVO;
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

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.util.List;

/**
 * @Name: RiskController
 * @Description: 失效风险计算接口（V1.0 计算公式待接入）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Api(tags = "失效风险计算")
@Validated
@RestController
@RequestMapping("/risk")
public class RiskController {

    private final IRiskAssessInterFace assessService;

    public RiskController(IRiskAssessInterFace assessService) {
        this.assessService = assessService;
    }

    @ApiOperation("风险因素定义")
    @GetMapping("/factors")
    public ApiResult<List<RiskFactorGroupVO>> listFactors() {
        return ApiResult.success(assessService.listFactors());
    }

    @ApiOperation("风险矩阵配置")
    @GetMapping("/matrix")
    public ApiResult<RiskMatrixVO> getMatrix() {
        return ApiResult.success(assessService.getMatrix());
    }

    @ApiOperation("管段风险概况")
    @GetMapping("/segments")
    public ApiResult<List<RiskSegmentVO>> listSegments() {
        return ApiResult.success(assessService.listSegments());
    }

    @ApiOperation("预填风险因素")
    @GetMapping("/segments/{segmentId}/inputs")
    public ApiResult<RiskInputsVO> getInputs(@PathVariable("segmentId") Long segmentId,
            @RequestParam(value = "evaluateYears", required = false)
            @DecimalMin(value = "0.01", message = "评价年限须大于0") @DecimalMax(value = "50", message = "评价年限不超过50年")
                    Double evaluateYears) {
        return ApiResult.success(assessService.getInputs(segmentId, evaluateYears));
    }

    @ApiOperation("失效风险评价")
    @PostMapping("/assess")
    public ApiResult<AssessmentVO> assess(@Valid @RequestBody AssessRequestDTO request) {
        return ApiResult.success(assessService.assess(request));
    }

    @ApiOperation("评价记录")
    @GetMapping("/assessments")
    public ApiResult<List<AssessmentVO>> listAssessments(
            @RequestParam(value = "segmentId", required = false) Long segmentId) {
        return ApiResult.success(assessService.listAssessments(segmentId));
    }
}
