package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

/**
 * @Name: AssessmentVO
 * @Description: 失效风险评价结果
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("失效风险评价结果")
public class AssessmentVO {

    @ApiModelProperty("评价ID")
    private String assessmentId;

    @ApiModelProperty("管段ID")
    private Long segmentId;

    @ApiModelProperty("管段")
    private String segmentName;

    @ApiModelProperty("评价时间")
    private String assessTime;

    @ApiModelProperty("评价年限")
    private Double evaluateYears;

    @ApiModelProperty("计算模型")
    private String modelCode;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("失效可能性等级")
    private Integer pofLevel;

    @ApiModelProperty("失效后果等级")
    private Integer cofLevel;

    @ApiModelProperty("失效可能性值")
    private Double pofValue;

    @ApiModelProperty("失效后果值")
    private Double cofValue;

    @ApiModelProperty("风险等级")
    private String riskLevel;

    @ApiModelProperty("风险等级名称")
    private String riskLevelName;

    @ApiModelProperty("说明")
    private String message;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("输入快照")
    private Map<String, Object> inputs = new LinkedHashMap<>();
}
