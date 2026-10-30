package com.petrochina.xnyqt.pims.corrosion.type;

/**
 * @Name: TrendDirectionType
 * @Description: 双期最大深度变化方向
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum TrendDirectionType {

    //第二期大于第一期
    INCREASE("增长"),
    //第二期小于第一期
    DECREASE("下降"),
    //两期相等
    FLAT("持平");

    private final String displayName;

    TrendDirectionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
