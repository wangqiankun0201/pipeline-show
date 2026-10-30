package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Data;

/**
 * @Name: InspectionVO
 * @Description: 内腐蚀检测记录
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("内腐蚀检测记录")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class InspectionVO {

    @ApiModelProperty("记录ID")
    private Long inspectionId;

    @ApiModelProperty("源管段ID")
    private Long sourceSegmentId;

    @ApiModelProperty("管段")
    private String segmentName;

    @ApiModelProperty("内检日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inspectionDate;

    @ApiModelProperty("检测时服役年限(年)")
    private Double serviceYears;

    @ApiModelProperty("外径(mm)")
    private Double outerDiameterMm;

    @ApiModelProperty("壁厚(mm)")
    private Double wallThicknessMm;

    @ApiModelProperty("材质等级")
    private String materialGrade;

    @ApiModelProperty("设计压力(MPa)")
    private Double designPressureMpa;

    @ApiModelProperty("运行压力(MPa)")
    private Double operatingPressureMpa;

    @ApiModelProperty("运行压力原始值")
    private String operatingPressureText;

    @ApiModelProperty("压力系数")
    private Double pressureFactor;

    @ApiModelProperty("运行温度")
    private String operatingTemperature;

    @ApiModelProperty("介质类型")
    private String mediumType;

    @ApiModelProperty("是否积液")
    private String liquidAccumulation;

    @ApiModelProperty("CO2(mol%)")
    private Double co2MolPct;

    @ApiModelProperty("H2S(mol%)")
    private Double h2sMolPct;

    @ApiModelProperty("O2(mol%)")
    private Double o2MolPct;

    @ApiModelProperty("含水率(%)")
    private Double waterCutPct;

    @ApiModelProperty("Cl-(mg/L)")
    private Double chlorideMgL;

    @ApiModelProperty("pH")
    private Double ph;

    @ApiModelProperty("缓蚀剂投加")
    private String inhibitorDosing;

    @ApiModelProperty("缓蚀剂浓度")
    private String inhibitorConcentration;

    @ApiModelProperty("清管周期(天)")
    private Integer piggingCycleDays;

    @ApiModelProperty("内腐蚀缺陷数量")
    private Integer defectCount;

    @ApiModelProperty("最大腐蚀深度(%)")
    private Double maxDepthPct;

    @ApiModelProperty("最大腐蚀深度(mm)")
    private Double maxDepthMm;

    @ApiModelProperty("平均ERF")
    private Double avgErf;

    @ApiModelProperty("最大ERF")
    private Double maxErf;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("是否为参数锚点（最近有效内检）")
    private Boolean isLatest;
}
