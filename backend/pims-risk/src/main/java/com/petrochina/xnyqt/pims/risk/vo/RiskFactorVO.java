package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @Name: RiskFactorVO
 * @Description: 风险因素
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("风险因素")
public class RiskFactorVO {

    @ApiModelProperty("因素编码")
    private String code;

    @ApiModelProperty("因素名称")
    private String name;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("输入类型 number/select")
    private String inputType;

    @ApiModelProperty("数据来源")
    private String source;

    @ApiModelProperty("数据来源名称")
    private String sourceName;

    @ApiModelProperty("选项")
    private List<FactorOptionVO> options = new ArrayList<>();

    @ApiModelProperty("是否必填")
    private Boolean required;

    @ApiModelProperty("最小值")
    private Double min;

    @ApiModelProperty("最大值")
    private Double max;
}
