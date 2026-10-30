package com.petrochina.xnyqt.pims.risk.feign.dto;

import lombok.Data;

/**
 * @Name: CorrosionSegmentDTO
 * @Description: 内腐蚀管段汇总（远程）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
public class CorrosionSegmentDTO {

    //管段ID
    private Long segmentId;

    //气矿
    private String mine;

    //管线
    private String pipelineName;

    //管段
    private String segmentName;

    //投运日期（直接接收腐蚀服务原始日期）
    private java.time.LocalDate commissionDate;

    //壁厚
    private Double wallThicknessMm;

    //是否可预测
    private Boolean predictable;
}
