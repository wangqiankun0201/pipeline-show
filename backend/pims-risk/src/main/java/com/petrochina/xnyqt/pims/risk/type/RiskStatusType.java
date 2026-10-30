package com.petrochina.xnyqt.pims.risk.type;

/**
 * @Name: RiskStatusType
 * @Description: 失效风险评价状态
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum RiskStatusType {

    //尚未评价
    NOT_ASSESSED("未评价"),
    //计算模型未接入，仅保存输入快照
    MODEL_PENDING("模型待接入"),
    //计算完成
    COMPLETED("已完成"),
    //计算失败
    FAILED("计算失败");

    private final String displayName;

    RiskStatusType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String nameOf(String code) {
        for (RiskStatusType type : values()) {
            if (type.name().equals(code)) {
                return type.displayName;
            }
        }
        return code;
    }
}
