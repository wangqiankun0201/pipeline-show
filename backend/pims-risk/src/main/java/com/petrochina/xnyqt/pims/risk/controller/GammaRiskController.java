package com.petrochina.xnyqt.pims.risk.controller;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.risk.dto.GammaPredictRequestDTO;
import com.petrochina.xnyqt.pims.risk.service.GammaRiskService;
import com.petrochina.xnyqt.pims.risk.vo.GammaPredictionVO;
import com.petrochina.xnyqt.pims.risk.vo.GammaSegmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "Gamma穿透风险")
@RestController
@RequestMapping("/risk/gamma")
public class GammaRiskController {
    private final GammaRiskService service;

    public GammaRiskController(GammaRiskService service) { this.service = service; }

    @ApiOperation("三段固定参数Gamma模型选择数据")
    @GetMapping("/segments")
    public ApiResult<List<GammaSegmentVO>> segments() {
        return ApiResult.success(service.listSegments());
    }

    @ApiOperation("最新内检条件Gamma穿透概率及路径模拟")
    @PostMapping("/predict")
    public ApiResult<GammaPredictionVO> predict(@Valid @RequestBody GammaPredictRequestDTO request) {
        return ApiResult.success(service.predict(request));
    }
}
