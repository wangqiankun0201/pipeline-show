package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: InspectionPointVO
 * @Description: 内检实测点
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("内检实测点")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class InspectionPointVO {

    @ApiModelProperty("内检日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @ApiModelProperty("自投运起年数")
    private Double tYears;

    @ApiModelProperty("最大深度(mm)")
    private Double depthMm;

    @ApiModelProperty("最大深度(%)")
    private Double depthPct;

    @ApiModelProperty("是否为锚点")
    private Boolean isAnchor;
}
