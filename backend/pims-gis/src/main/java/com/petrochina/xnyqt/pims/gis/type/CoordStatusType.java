package com.petrochina.xnyqt.pims.gis.type;

/**
 * @Name: CoordStatusType
 * @Description: 管段坐标状态
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum CoordStatusType {

    //示意坐标，待正式 GIS 数据替换
    SCHEMATIC("示意坐标"),
    //实测/正式 GIS 坐标
    SURVEYED("实测坐标");

    private final String displayName;

    CoordStatusType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @Name: nameOf
     * @Description: 根据编码获取显示名称，未知编码返回原值
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: String code 编码
     * @Return: String 显示名称
     */
    public static String nameOf(String code) {
        for (CoordStatusType type : values()) {
            if (type.name().equals(code)) {
                return type.displayName;
            }
        }
        return code;
    }
}
