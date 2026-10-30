package com.petrochina.xnyqt.pims.risk.feign.fallback;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.risk.feign.CorrosionFeignClient;
import com.petrochina.xnyqt.pims.risk.feign.dto.CorrosionSegmentDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.InspectionDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.PredictSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Name: CorrosionFeignFallbackFactory
 * @Description: 内腐蚀服务调用降级（Sentinel 熔断或服务不可用时返回失败结果）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Component
public class CorrosionFeignFallbackFactory implements FallbackFactory<CorrosionFeignClient> {

    //降级提示
    private static final String MESSAGE = "内腐蚀服务暂不可用";

    @Override
    public CorrosionFeignClient create(Throwable cause) {
        log.warn("{}：{}", MESSAGE, cause.getMessage());
        return new CorrosionFeignClient() {
            @Override
            public ApiResult<List<CorrosionSegmentDTO>> listSegments() {
                return ApiResult.fail(MESSAGE);
            }

            @Override
            public ApiResult<List<PredictSummaryDTO>> summarizeAll(Double yearsAfterIli) {
                return ApiResult.fail(MESSAGE);
            }

            @Override
            public ApiResult<List<InspectionDTO>> listInspections(Long segmentId) {
                return ApiResult.fail(MESSAGE);
            }
        };
    }
}
