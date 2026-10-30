package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: CorrosionSegmentVO
 * @Description: 管段腐蚀汇总
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("管段腐蚀汇总")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class CorrosionSegmentVO {

    @ApiModelProperty("物理管段ID")
    private Long segmentId;

    @ApiModelProperty("气矿")
    private String mine;

    @ApiModelProperty("作业区")
    private String workArea;

    @ApiModelProperty("管线")
    private String pipelineName;

    @ApiModelProperty("管段")
    private String segmentName;

    @ApiModelProperty("投运日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate commissionDate;

    @ApiModelProperty("名义壁厚(mm)")
    private Double wallThicknessMm;

    @ApiModelProperty("归并源管段ID")
    private String sourceSegmentIds;

    @ApiModelProperty("检测记录数")
    private Integer inspectionCount;

    @ApiModelProperty("最近有效内检日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate latestInspectionDate;

    @ApiModelProperty("最近内检最大腐蚀深度(%)")
    private Double latestMaxDepthPct;

    @ApiModelProperty("d0(mm)")
    private Double d0Mm;

    @ApiModelProperty("T(年)")
    private Double tYears;

    @ApiModelProperty("k（基准 n）")
    private Double kValue;

    @ApiModelProperty("是否可预测")
    private Boolean predictable;

    @ApiModelProperty("数据可信等级")
    private String grade;

    @ApiModelProperty("可信等级名称")
    private String gradeName;

    @ApiModelProperty("说明")
    private String note;
}
