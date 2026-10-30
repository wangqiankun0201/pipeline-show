package com.petrochina.xnyqt.pims.corrosion.type;

/**
 * @Name: TimeOriginType
 * @Description: 时间原点口径
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum TimeOriginType {

    //腐蚀起始日期未知时以投运日期替代
    COMMISSION_DATE("投运日期替代（试算模式）"),
    //经现场核实的腐蚀起始日期
    CORROSION_START("实证腐蚀起始日期");

    private final String displayName;

    TimeOriginType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
