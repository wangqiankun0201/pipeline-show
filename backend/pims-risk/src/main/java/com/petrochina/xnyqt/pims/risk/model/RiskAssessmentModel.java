package com.petrochina.xnyqt.pims.risk.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @Name: RiskAssessmentModel
 * @Description: 失效风险评价记录，对应表 t_risk_assessment
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@TableName("t_risk_assessment")
public class RiskAssessmentModel {

    //评价ID
    @TableId(type = IdType.INPUT)
    private String assessmentId;

    //管段ID
    private Long segmentId;

    //管段名称
    private String segmentName;

    //评价年限（内检后年数）
    private Double evaluateYears;

    //计算模型编码
    private String modelCode;

    //状态
    private String status;

    //失效可能性等级
    private Integer pofLevel;

    //失效后果等级
    private Integer cofLevel;

    //失效可能性值
    private Double pofValue;

    //失效后果值
    private Double cofValue;

    //风险等级
    private String riskLevel;

    //计算说明
    private String message;

    //输入快照 JSON
    private String inputsJson;

    //操作人
    private String operator;

    //评价时间
    private LocalDateTime assessTime;
}
