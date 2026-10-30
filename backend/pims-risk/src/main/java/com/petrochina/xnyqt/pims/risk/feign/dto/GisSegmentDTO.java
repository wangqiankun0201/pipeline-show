package com.petrochina.xnyqt.pims.risk.feign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: GisSegmentDTO
 * @Description: 管段基础数据（远程）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
public class GisSegmentDTO {

    //管段ID
    private Long segmentId;

    //管段
    private String segmentName;

    //投运日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate commissionDate;

    //外径
    private Double outerDiameterMm;

    //壁厚
    private Double wallThicknessMm;

    //材质
    private String materialGrade;

    //设计压力
    private Double designPressureMpa;

    //介质
    private String mediumType;
}
