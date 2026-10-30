package com.petrochina.xnyqt.pims.corrosion.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Name: CorrosionSegmentModel
 * @Description: 内腐蚀物理管段（同一“气矿—管线—管段”归并），对应表 t_corrosion_segment
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@TableName("t_corrosion_segment")
public class CorrosionSegmentModel {

    //物理管段ID：归并到最小源管段ID
    @TableId(type = IdType.INPUT)
    private Long segmentId;

    private String mine;

    private String workArea;

    private String pipelineName;

    private String segmentName;

    private LocalDate commissionDate;

    private Double outerDiameterMm;

    //名义壁厚（取最近一条有壁厚的记录）
    private Double wallThicknessMm;

    //归并的源管段ID，逗号分隔
    private String sourceSegmentIds;

    private String importBatch;

    private LocalDateTime updateTime;
}
