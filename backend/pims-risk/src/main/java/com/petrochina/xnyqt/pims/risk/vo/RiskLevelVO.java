package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Name: RiskLevelVO
 * @Description: 风险等级
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("风险等级")
public class RiskLevelVO {

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("颜色")
    private String color;
}
