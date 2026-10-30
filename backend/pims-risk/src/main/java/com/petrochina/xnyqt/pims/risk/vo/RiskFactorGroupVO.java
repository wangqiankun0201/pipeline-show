package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @Name: RiskFactorGroupVO
 * @Description: 风险因素分组
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("风险因素分组")
public class RiskFactorGroupVO {

    @ApiModelProperty("分组编码")
    private String groupCode;

    @ApiModelProperty("分组名称")
    private String groupName;

    @ApiModelProperty("因素")
    private List<RiskFactorVO> factors = new ArrayList<>();
}
