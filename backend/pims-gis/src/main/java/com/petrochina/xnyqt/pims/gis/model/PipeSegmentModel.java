package com.petrochina.xnyqt.pims.gis.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Name: PipeSegmentModel
 * @Description: 管段基础信息，对应表 t_pipe_segment
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@TableName("t_pipe_segment")
public class PipeSegmentModel {

    //管段ID（与内腐蚀数据源管段ID一致）
    @TableId(type = IdType.INPUT)
    private Long segmentId;

    private String mine;

    private String workArea;

    private String pipelineName;

    private String segmentName;

    private LocalDate commissionDate;

    private Double outerDiameterMm;

    private Double wallThicknessMm;

    private String materialGrade;

    private Double designPressureMpa;

    private String mediumType;

    private String dataSource;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
