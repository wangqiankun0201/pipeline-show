package com.petrochina.xnyqt.pims.risk.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

/**
 * @Name: RiskCalcContextDTO
 * @Description: 风险计算上下文
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
public class RiskCalcContextDTO {

    //管段ID
    private Long segmentId;

    //评价年限
    private Double evaluateYears;

    //因素取值
    private Map<String, Object> inputs = new LinkedHashMap<>();
}
