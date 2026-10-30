package com.petrochina.xnyqt.pims.corrosion.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Name: CorrosionModelParamModel
 * @Description: 幂律模型参数库（k 与 n 成组存储，禁止混用），对应表 t_corrosion_model_param
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@TableName("t_corrosion_model_param")
public class CorrosionModelParamModel {

    @TableId(type = IdType.AUTO)
    private Long paramId;

    private Long segmentId;

    private String modelName;

    private String modelVersion;

    //幂律增长指数 n
    private Double exponentN;

    //腐蚀发展系数 k（mm/年ⁿ）
    private Double coefficientK;

    private Double d0Mm;

    //投运至锚点内检年数 T
    private Double anchorYears;

    //锚点：最近一次有效内检日期
    private LocalDate anchorDate;

    private LocalDate commissionDate;

    private Double wallThicknessMm;

    private Double maxDepthPct;

    //锚点检测记录的源管段ID
    private Long anchorSourceSegmentId;

    private String timeOrigin;

    private String grade;

    private String parameterSource;

    private LocalDateTime calcTime;
}
