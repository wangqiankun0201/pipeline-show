package com.petrochina.xnyqt.pims.corrosion.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Name: CorrosionPredictLogModel
 * @Description: 预测计算留痕（计算版本、参数快照、操作人、时间），对应表 t_corrosion_predict_log
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@TableName("t_corrosion_predict_log")
public class CorrosionPredictLogModel {

    @TableId(type = IdType.INPUT)
    private String calcId;

    private Long segmentId;

    private String modelVersion;

    //幂律增长指数 n
    private Double exponentN;

    //腐蚀发展系数 k（mm/年ⁿ）
    private Double coefficientK;

    private Double d0Mm;

    //投运至锚点内检年数 T
    private Double anchorYears;

    private LocalDate anchorDate;

    private LocalDate targetDate;

    private Double deltaYears;

    private Double predDepthMm;

    private Double depthRatio;

    private String limitFlag;

    private String requestJson;

    private String operator;

    private LocalDateTime calcTime;
}
