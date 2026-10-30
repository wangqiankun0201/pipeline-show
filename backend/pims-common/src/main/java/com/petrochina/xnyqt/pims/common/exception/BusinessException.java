package com.petrochina.xnyqt.pims.common.exception;

/**
 * @Name: BusinessException
 * @Description: 业务异常，消息内容将直接返回前端，禁止包含调试信息
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
