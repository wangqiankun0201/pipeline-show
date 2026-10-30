package com.petrochina.xnyqt.pims.gis.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Name: PipeSegmentGeometryModel
 * @Description: 管段空间数据（GeoJSON，WGS84），对应表 t_pipe_segment_geometry
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@TableName("t_pipe_segment_geometry")
public class PipeSegmentGeometryModel {

    @TableId(type = IdType.INPUT)
    private Long segmentId;

    //几何类型：LineString / MultiLineString
    private String geomType;

    //GeoJSON 几何对象文本
    private String geojson;

    //坐标系，固定 4326
    private Integer srid;

    private String startName;

    private String endName;

    private Double startLon;

    private Double startLat;

    private Double endLon;

    private Double endLat;

    private Double lengthKm;

    //坐标状态：SCHEMATIC 示意 / SURVEYED 实测
    private String coordStatus;

    private String geoSource;

    private LocalDateTime updateTime;
}
