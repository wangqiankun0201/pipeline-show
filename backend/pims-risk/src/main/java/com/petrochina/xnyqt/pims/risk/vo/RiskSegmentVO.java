package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Name: RiskSegmentVO
 * @Description: 管段风险概况
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("管段风险概况")
public class RiskSegmentVO {

    @ApiModelProperty("管段ID")
    private Long segmentId;

    @ApiModelProperty("气矿")
    private String mine;

    @ApiModelProperty("管线")
    private String pipelineName;

    @ApiModelProperty("管段")
    private String segmentName;

    @ApiModelProperty("预测腐蚀深度(mm)")
    private Double predDepthMm;

    @ApiModelProperty("预测深度比")
    private Double depthRatio;

    @ApiModelProperty("腐蚀越界标识")
    private String corrosionLimitFlag;

    @ApiModelProperty("失效可能性等级")
    private Integer pofLevel;

    @ApiModelProperty("失效后果等级")
    private Integer cofLevel;

    @ApiModelProperty("风险等级")
    private String riskLevel;

    @ApiModelProperty("风险等级名称")
    private String riskLevelName;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("最近评价时间")
    private String lastAssessTime;
}
