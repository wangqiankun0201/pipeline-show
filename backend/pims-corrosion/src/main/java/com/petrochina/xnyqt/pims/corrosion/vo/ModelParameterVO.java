package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: ModelParameterVO
 * @Description: 幂律模型参数
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("幂律模型参数")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ModelParameterVO {

    @ApiModelProperty("管段ID")
    private Long segmentId;

    @ApiModelProperty("管段")
    private String segmentName;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("模型版本")
    private String modelVersion;

    @ApiModelProperty("增长指数 n")
    private Double n;

    @ApiModelProperty("腐蚀发展系数 k")
    private Double kValue;

    @ApiModelProperty("k 单位")
    private String kUnit;

    @ApiModelProperty("d0(mm)")
    private Double d0Mm;

    @ApiModelProperty("T(年)")
    private Double tYears;

    @ApiModelProperty("锚点最大腐蚀深度(%)")
    private Double maxDepthPct;

    @ApiModelProperty("壁厚(mm)")
    private Double wallThicknessMm;

    @ApiModelProperty("锚点内检日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate anchorDate;

    @ApiModelProperty("投运日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate commissionDate;

    @ApiModelProperty("时间原点")
    private String timeOrigin;

    @ApiModelProperty("时间原点说明")
    private String timeOriginName;

    @ApiModelProperty("可信等级")
    private String grade;

    @ApiModelProperty("可信等级名称")
    private String gradeName;

    @ApiModelProperty("参数来源")
    private String parameterSource;
}
