package com.petrochina.xnyqt.pims.risk.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Name: AssessRequestDTO
 * @Description: 失效风险评价请求
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("失效风险评价请求")
public class AssessRequestDTO {

    @ApiModelProperty(value = "管段ID", required = true)
    @NotNull(message = "管段ID不能为空")
    private Long segmentId;

    @ApiModelProperty("评价年限（内检后年数），默认 5")
    @DecimalMin(value = "0.01", message = "评价年限须大于0")
    @DecimalMax(value = "50", message = "评价年限不超过50年")
    private Double evaluateYears;

    @ApiModelProperty("因素取值 {因素编码: 值}")
    @Size(max = 100, message = "因素数量超出限制")
    private Map<String, Object> inputs = new LinkedHashMap<>();
}
