package com.petrochina.xnyqt.pims.risk.calculator.impl;

import com.petrochina.xnyqt.pims.risk.calculator.IRiskCalculateInterFace;
import com.petrochina.xnyqt.pims.risk.calculator.RiskCalcContextDTO;
import com.petrochina.xnyqt.pims.risk.calculator.RiskCalcResultDTO;
import com.petrochina.xnyqt.pims.risk.type.RiskStatusType;
import org.springframework.stereotype.Component;

/**
 * @Name: PendingRiskCalculateImpl
 * @Description: 占位计算模型：公式待接入，不输出任何风险数值，仅返回“模型待接入”状态
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Component
public class PendingRiskCalculateImpl implements IRiskCalculateInterFace {

    //占位模型编码
    public static final String MODEL_CODE = "PENDING";

    @Override
    public String getModelCode() {
        return MODEL_CODE;
    }

    @Override
    public RiskCalcResultDTO calculate(RiskCalcContextDTO context) {
        RiskCalcResultDTO result = new RiskCalcResultDTO();
        result.setStatus(RiskStatusType.MODEL_PENDING.name());
        result.setMessage("失效风险计算公式待接入，已保存输入快照（" + context.getInputs().size() + " 项因素）。");
        return result;
    }
}
