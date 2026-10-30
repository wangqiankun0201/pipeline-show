package com.petrochina.xnyqt.pims.corrosion.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Name: CorrosionInspectionModel
 * @Description: 内腐蚀检测记录（对应《内腐蚀数据.xlsx》一行），对应表 t_corrosion_inspection
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@TableName("t_corrosion_inspection")
public class CorrosionInspectionModel {

    @TableId(type = IdType.AUTO)
    private Long inspectionId;

    //物理管段ID
    private Long segmentId;

    //源数据管段ID
    private Long sourceSegmentId;

    //源文件行号
    private Integer rowNo;

    private String mine;

    private String workArea;

    private String pipelineName;

    private String segmentName;

    private LocalDate commissionDate;

    private LocalDate inspectionDate;

    private Double outerDiameterMm;

    private Double wallThicknessMm;

    private String materialGrade;

    private Double designPressureMpa;

    //运行压力（可解析为单值时）
    private Double operatingPressureMpa;

    //运行压力原始文本（如区间“4.9~5.1”）
    private String operatingPressureText;

    private Double pressureFactor;

    private String operatingTemperature;

    private String mediumType;

    private String liquidAccumulation;

    private Double co2MolPct;

    private Double h2sMolPct;

    private Double o2MolPct;

    private Double waterCutPct;

    private Double chlorideMgL;

    private Double ph;

    private String inhibitorDosing;

    private String inhibitorConcentration;

    private Integer piggingCycleDays;

    private Integer defectCount;

    private Double maxDepthPct;

    private Double avgErf;

    private Double maxErf;

    private String remark;

    private String importBatch;

    private LocalDateTime createTime;
}
