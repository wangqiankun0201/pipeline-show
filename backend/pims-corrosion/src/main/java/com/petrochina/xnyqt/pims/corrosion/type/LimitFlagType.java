package com.petrochina.xnyqt.pims.corrosion.type;

/**
 * @Name: LimitFlagType
 * @Description: 预测结果越界标识
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum LimitFlagType {

    //预测深度低于关注阈值
    NORMAL("正常"),
    //预测深度达到关注阈值（默认80%壁厚）
    ATTENTION("接近壁厚"),
    //预测深度达到名义壁厚，停止物理外推
    MODEL_OUT_OF_RANGE("模型越界");

    private final String displayName;

    LimitFlagType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
