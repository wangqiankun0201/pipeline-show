package com.petrochina.xnyqt.pims.common.type;

/**
 * @Name: ResultCodeType
 * @Description: 统一返回码类型，与梦想云用户中心返回结构保持一致
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public enum ResultCodeType {

    //成功
    SUCCESS("success", "操作成功"),
    //业务失败
    FAIL("fail", "操作失败"),
    //未登录或令牌失效
    UNAUTHORIZED("unauthorized", "登录已失效，请重新登录");

    private final String code;

    private final String message;

    ResultCodeType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
