package com.petrochina.xnyqt.pims.risk.type;

/**
 * @Name: RiskLevelType
 * @Description: 风险等级（颜色取梦想云 UI 规范状态色）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum RiskLevelType {

    //低风险：成功色
    LOW("低风险", "#3BC243"),
    //中风险：提醒色
    MEDIUM("中风险", "#FF9E0F"),
    //较高风险：重要色
    HIGH("较高风险", "#FE377E"),
    //高风险：错误色
    VERY_HIGH("高风险", "#F35159");

    private final String displayName;

    private final String color;

    RiskLevelType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public static String nameOf(String code) {
        for (RiskLevelType type : values()) {
            if (type.name().equals(code)) {
                return type.displayName;
            }
        }
        return null;
    }
}
