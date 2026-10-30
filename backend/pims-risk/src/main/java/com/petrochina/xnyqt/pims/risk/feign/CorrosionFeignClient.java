package com.petrochina.xnyqt.pims.risk.feign;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.risk.feign.dto.CorrosionSegmentDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.InspectionDTO;
import com.petrochina.xnyqt.pims.risk.feign.dto.PredictSummaryDTO;
import com.petrochina.xnyqt.pims.risk.feign.fallback.CorrosionFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Name: CorrosionFeignClient
 * @Description: 内腐蚀服务远程调用（url 为空时经 Nacos 服务发现负载均衡）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@FeignClient(name = "pims-corrosion", contextId = "corrosionFeignClient", url = "${pims.feign.corrosion-url:}",
        fallbackFactory = CorrosionFeignFallbackFactory.class)
public interface CorrosionFeignClient {

    /**
     * @Name: listSegments
     * @Description: 管段腐蚀汇总
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: ApiResult 管段汇总
     */
    @GetMapping("/corrosion/segments")
    ApiResult<List<CorrosionSegmentDTO>> listSegments();

    /**
     * @Name: summarizeAll
     * @Description: 全部管段内检后指定年数的预测摘要
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Double yearsAfterIli 内检后年数
     * @Return: ApiResult 预测摘要
     */
    @GetMapping("/corrosion/inner/predict-summary")
    ApiResult<List<PredictSummaryDTO>> summarizeAll(@RequestParam("yearsAfterIli") Double yearsAfterIli);

    /**
     * @Name: listInspections
     * @Description: 管段内检记录
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Long segmentId 管段ID
     * @Return: ApiResult 内检记录
     */
    @GetMapping("/corrosion/segments/{segmentId}/inspections")
    ApiResult<List<InspectionDTO>> listInspections(@PathVariable("segmentId") Long segmentId);
}
