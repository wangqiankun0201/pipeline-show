package com.petrochina.xnyqt.pims.risk.feign.fallback;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.risk.feign.GisFeignClient;
import com.petrochina.xnyqt.pims.risk.feign.dto.GisSegmentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @Name: GisFeignFallbackFactory
 * @Description: 管道位置服务调用降级
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Component
public class GisFeignFallbackFactory implements FallbackFactory<GisFeignClient> {

    //降级提示
    private static final String MESSAGE = "管道位置服务暂不可用";

    @Override
    public GisFeignClient create(Throwable cause) {
        log.warn("{}：{}", MESSAGE, cause.getMessage());
        return segmentId -> ApiResult.<GisSegmentDTO>fail(MESSAGE);
    }
}
