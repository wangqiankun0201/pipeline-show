package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @Name: ScenarioResultVO
 * @Description: 情景预测结果
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("情景预测结果")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ScenarioResultVO {

    @ApiModelProperty("增长指数 n")
    private Double n;

    @ApiModelProperty("k")
    private Double kValue;

    @ApiModelProperty("预测深度(mm)")
    private Double predDepthMm;

    @ApiModelProperty("深度比（占名义壁厚）")
    private Double depthRatio;

    @ApiModelProperty("剩余壁厚(mm)")
    private Double remainingWallMm;

    @ApiModelProperty("越界标识")
    private String limitFlag;

    @ApiModelProperty("越界标识名称")
    private String limitFlagName;

    @ApiModelProperty("达到名义壁厚日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate outOfRangeDate;

    @ApiModelProperty("趋势曲线")
    private List<CurvePointVO> curve = new ArrayList<>();
}
