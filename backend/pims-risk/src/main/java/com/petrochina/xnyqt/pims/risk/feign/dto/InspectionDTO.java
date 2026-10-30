package com.petrochina.xnyqt.pims.risk.feign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: InspectionDTO
 * @Description: 内检记录（远程）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
public class InspectionDTO {

    //内检日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inspectionDate;

    //原始内检深度百分比与该次内检壁厚，用于恢复未四舍五入的深度
    private Double maxDepthPct;
    private Double wallThicknessMm;
    private Double maxDepthMm;

    //运行压力
    private Double operatingPressureMpa;

    //H2S
    private Double h2sMolPct;

    //CO2
    private Double co2MolPct;

    //缓蚀剂投加
    private String inhibitorDosing;

    //清管周期
    private Integer piggingCycleDays;
}
