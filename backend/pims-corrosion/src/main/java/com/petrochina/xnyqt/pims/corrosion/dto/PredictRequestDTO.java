package com.petrochina.xnyqt.pims.corrosion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @Name: PredictRequestDTO
 * @Description: 内腐蚀趋势预测请求（targetDate 与 yearsAfterIli 二选一，均为空默认内检后 5 年）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("内腐蚀趋势预测请求")
public class PredictRequestDTO {

    @ApiModelProperty(value = "管段ID", required = true)
    @NotNull(message = "管段ID不能为空")
    private Long segmentId;

    @ApiModelProperty("目标日期，须晚于最近内检日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @ApiModelProperty("内检后年数")
    @DecimalMin(value = "0.01", message = "内检后年数须大于0")
    @DecimalMax(value = "50", message = "内检后年数不超过50年")
    private Double yearsAfterIli;

    @ApiModelProperty("兼容字段：仅允许该管段固定 n，其他值拒绝")
    @DecimalMin(value = "0.1", message = "增长指数 n 须不小于0.1")
    @DecimalMax(value = "3.0", message = "增长指数 n 须不大于3.0")
    private Double scenarioN;

    @ApiModelProperty("兼容字段：不再执行对比情景，响应 scenarios 为空数组")
    private Boolean compareScenarios;

    @ApiModelProperty("曲线延伸至内检后年数")
    @DecimalMin(value = "1", message = "曲线延伸年数须不小于1")
    @DecimalMax(value = "50", message = "曲线延伸年数不超过50年")
    private Double curveEndYears;
}
