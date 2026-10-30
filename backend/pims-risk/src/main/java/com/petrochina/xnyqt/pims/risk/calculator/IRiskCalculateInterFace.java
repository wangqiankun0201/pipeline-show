package com.petrochina.xnyqt.pims.risk.calculator;

/**
 * @Name: IRiskCalculateInterFace
 * @Description: 失效风险计算策略接口。业务公式确定后新增实现类并配置 pims.risk.model-code 即可切换，
 *               无需改动接口、页面与评价记录结构
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public interface IRiskCalculateInterFace {

    /**
     * @Name: getModelCode
     * @Description: 计算模型编码（与配置 pims.risk.model-code 对应）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: String 模型编码
     */
    String getModelCode();

    /**
     * @Name: calculate
     * @Description: 根据因素取值计算失效可能性、失效后果与风险等级
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: RiskCalcContextDTO context 计算上下文
     * @Return: RiskCalcResultDTO 计算结果
     */
    RiskCalcResultDTO calculate(RiskCalcContextDTO context);
}
