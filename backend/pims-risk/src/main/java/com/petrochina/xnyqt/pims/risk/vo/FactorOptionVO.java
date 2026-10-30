package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Name: FactorOptionVO
 * @Description: 选项
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("选项")
public class FactorOptionVO {

    @ApiModelProperty("显示名称")
    private String label;

    @ApiModelProperty("取值")
    private String value;
}
