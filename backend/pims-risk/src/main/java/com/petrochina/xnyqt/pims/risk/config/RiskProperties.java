package com.petrochina.xnyqt.pims.risk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Name: RiskProperties
 * @Description: 失效风险计算配置
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ConfigurationProperties(prefix = "pims.risk")
public class RiskProperties {

    //启用的计算模型编码，对应 IRiskCalculateInterFace#getModelCode
    private String modelCode = "PENDING";

    //默认评价年限（内检后年数）
    private double defaultEvaluateYears = 5.0;

    //评价记录查询条数上限
    private int historyLimit = 50;
}
