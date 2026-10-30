package com.petrochina.xnyqt.pims.corrosion.type;

/**
 * @Name: GradeType
 * @Description: 数据可信等级（技术说明 6.1）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum GradeType {

    //多期缺陷已配准
    A("缺陷匹配"),
    //管段具有两期检测
    B("两期管段"),
    //仅最近一次内检锚定
    C("单期锚定"),
    //关键字段缺失，不可计算
    D("缺失");

    private final String displayName;

    GradeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
