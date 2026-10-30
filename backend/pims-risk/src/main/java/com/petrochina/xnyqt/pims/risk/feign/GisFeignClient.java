package com.petrochina.xnyqt.pims.risk.feign;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.risk.feign.dto.GisSegmentDTO;
import com.petrochina.xnyqt.pims.risk.feign.fallback.GisFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Name: GisFeignClient
 * @Description: 管道位置服务远程调用
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@FeignClient(name = "pims-gis", contextId = "gisFeignClient", url = "${pims.feign.gis-url:}",
        fallbackFactory = GisFeignFallbackFactory.class)
public interface GisFeignClient {

    /**
     * @Name: getSegment
     * @Description: 管段基础数据
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Long segmentId 管段ID
     * @Return: ApiResult 管段
     */
    @GetMapping("/gis/segments/{segmentId}")
    ApiResult<GisSegmentDTO> getSegment(@PathVariable("segmentId") Long segmentId);
}
