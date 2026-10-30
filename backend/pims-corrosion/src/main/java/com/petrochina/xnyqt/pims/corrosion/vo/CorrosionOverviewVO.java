package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: CorrosionOverviewVO
 * @Description: 内腐蚀概览
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("内腐蚀概览")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class CorrosionOverviewVO {

    @ApiModelProperty("管段数")
    private Integer segmentCount;

    @ApiModelProperty("检测记录数")
    private Integer inspectionCount;

    @ApiModelProperty("可预测管段数")
    private Integer predictableCount;

    @ApiModelProperty("k 中位数（基准 n）")
    private Double medianK;

    @ApiModelProperty("最大腐蚀深度(%)")
    private Double maxDepthPct;

    @ApiModelProperty("最近内检日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate latestInspectionDate;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("模型版本")
    private String modelVersion;

    @ApiModelProperty("基准指数 n")
    private Double baseN;
}
