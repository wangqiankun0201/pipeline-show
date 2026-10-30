package com.petrochina.xnyqt.pims.risk.feign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: PredictSummaryDTO
 * @Description: 管段预测摘要（远程）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
public class PredictSummaryDTO {

    //管段ID
    private Long segmentId;

    //管段
    private String segmentName;

    //目标日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    //预测深度
    private Double predDepthMm;

    //深度比
    private Double depthRatio;

    //剩余壁厚
    private Double remainingWallMm;

    //越界标识
    private String limitFlag;
}
