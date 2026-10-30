package com.petrochina.xnyqt.pims.gis.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * @Name: SegmentVO
 * @Description: 管段位置信息视图对象
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("管段位置信息")
public class SegmentVO {

    @ApiModelProperty("管段ID")
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

    @ApiModelProperty("外径(mm)")
    private Double outerDiameterMm;

    @ApiModelProperty("壁厚(mm)")
    private Double wallThicknessMm;

    @ApiModelProperty("材质等级")
    private String materialGrade;

    @ApiModelProperty("设计压力(MPa)")
    private Double designPressureMpa;

    @ApiModelProperty("介质类型")
    private String mediumType;

    @ApiModelProperty("长度(km)")
    private Double lengthKm;

    @ApiModelProperty("起点名称")
    private String startName;

    @ApiModelProperty("终点名称")
    private String endName;

    @ApiModelProperty("起点经度")
    private Double startLon;

    @ApiModelProperty("起点纬度")
    private Double startLat;

    @ApiModelProperty("终点经度")
    private Double endLon;

    @ApiModelProperty("终点纬度")
    private Double endLat;

    @ApiModelProperty("坐标状态：SCHEMATIC 示意 / SURVEYED 实测")
    private String coordStatus;

    @ApiModelProperty("坐标状态名称")
    private String coordStatusName;

    @ApiModelProperty("数据来源")
    private String dataSource;

    @ApiModelProperty("GeoJSON 几何（WGS84）")
    private JsonNode geometry;
}
