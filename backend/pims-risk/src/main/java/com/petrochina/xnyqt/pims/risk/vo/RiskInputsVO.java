package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @Name: RiskInputsVO
 * @Description: 风险评价预填输入
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("风险评价预填输入")
public class RiskInputsVO {

    @ApiModelProperty("管段ID")
    private Long segmentId;

    @ApiModelProperty("因素取值")
    private Map<String, Object> values = new LinkedHashMap<>();

    @ApiModelProperty("预填说明")
    private List<String> messages = new ArrayList<>();
}
