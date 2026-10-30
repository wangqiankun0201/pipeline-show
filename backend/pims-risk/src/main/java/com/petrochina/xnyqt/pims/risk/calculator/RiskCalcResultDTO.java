package com.petrochina.xnyqt.pims.risk.calculator;

import lombok.Data;

/**
 * @Name: RiskCalcResultDTO
 * @Description: 风险计算结果
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
public class RiskCalcResultDTO {

    //状态
    private String status;

    //失效可能性值
    private Double pofValue;

    //失效后果值
    private Double cofValue;

    //失效可能性等级
    private Integer pofLevel;

    //失效后果等级
    private Integer cofLevel;

    //风险等级
    private String riskLevel;

    //说明
    private String message;
}
