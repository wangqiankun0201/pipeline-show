package com.petrochina.xnyqt.pims.risk.type;

/**
 * @Name: FactorSourceType
 * @Description: 风险因素数据来源
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum FactorSourceType {

    //由内腐蚀预测服务带入
    CORROSION("内腐蚀预测"),
    //由管道位置服务（管段基础数据）带入
    GIS("管段数据"),
    //人工录入
    MANUAL("人工录入");

    private final String displayName;

    FactorSourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String nameOf(String code) {
        for (FactorSourceType type : values()) {
            if (type.name().equals(code)) {
                return type.displayName;
            }
        }
        return code;
    }
}
