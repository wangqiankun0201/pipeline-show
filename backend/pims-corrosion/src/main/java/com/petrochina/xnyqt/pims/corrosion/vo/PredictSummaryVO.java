package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: PredictSummaryVO
 * @Description: 管段预测摘要（供失效风险服务调用）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("管段预测摘要（供失效风险服务调用）")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class PredictSummaryVO {

    @ApiModelProperty("管段ID")
    private Long segmentId;

    @ApiModelProperty("管段")
    private String segmentName;

    @ApiModelProperty("目标日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @ApiModelProperty("预测深度(mm)")
    private Double predDepthMm;

    @ApiModelProperty("深度比")
    private Double depthRatio;

    @ApiModelProperty("剩余壁厚(mm)")
    private Double remainingWallMm;

    @ApiModelProperty("越界标识")
    private String limitFlag;

    @ApiModelProperty("越界标识名称")
    private String limitFlagName;
}
