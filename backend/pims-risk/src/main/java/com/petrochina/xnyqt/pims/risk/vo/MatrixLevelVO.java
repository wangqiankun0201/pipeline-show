package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Name: MatrixLevelVO
 * @Description: 矩阵等级
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("矩阵等级")
public class MatrixLevelVO {

    @ApiModelProperty("等级")
    private Integer level;

    @ApiModelProperty("名称")
    private String name;
}
