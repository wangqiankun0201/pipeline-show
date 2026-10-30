package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: ApparentExponentVO
 * @Description: 双期记录表观指数
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("双期记录表观指数")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ApparentExponentVO {

    @ApiModelProperty("源管段ID")
    private Long sourceSegmentId;

    @ApiModelProperty("管段")
    private String segmentName;

    @ApiModelProperty("第一期日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date1;

    @ApiModelProperty("第二期日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date2;

    @ApiModelProperty("T1(年)")
    private Double t1Years;

    @ApiModelProperty("T2(年)")
    private Double t2Years;

    @ApiModelProperty("D1(%)")
    private Double d1Pct;

    @ApiModelProperty("D2(%)")
    private Double d2Pct;

    @ApiModelProperty("表观指数 n_app")
    private Double nApp;

    @ApiModelProperty("变化方向")
    private String direction;

    @ApiModelProperty("变化方向名称")
    private String directionName;

    @ApiModelProperty("数据判断")
    private String judgement;
}
