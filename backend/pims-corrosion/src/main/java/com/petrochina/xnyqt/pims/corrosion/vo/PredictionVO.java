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
 * @Name: PredictionVO
 * @Description: 内腐蚀趋势预测结果
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("内腐蚀趋势预测结果")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class PredictionVO {

    @ApiModelProperty("计算ID")
    private String calcId;

    @ApiModelProperty("计算时间")
    private String calcTime;

    @ApiModelProperty("模型参数")
    private ModelParameterVO parameter;

    @ApiModelProperty("目标日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @ApiModelProperty("距最近内检年数")
    private Double deltaYears;

    @ApiModelProperty("主情景结果")
    private ScenarioResultVO main;

    @ApiModelProperty("情景对比")
    private List<ScenarioResultVO> scenarios = new ArrayList<>();

    @ApiModelProperty("内检实测点")
    private List<InspectionPointVO> inspectionPoints = new ArrayList<>();

    @ApiModelProperty("提示信息")
    private List<String> warnings = new ArrayList<>();
}
