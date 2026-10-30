package com.petrochina.xnyqt.pims.common.web;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * @Name: GlobalExceptionHandler
 * @Description: 全局异常处理，按安全规范隐藏后台调试信息，仅返回可读提示
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusiness(BusinessException e) {
        return ApiResult.fail(e.getMessage());
    }

    /**
     * @Name: handleBind
     * @Description: 处理请求体/表单参数校验异常（MethodArgumentNotValidException 为 BindException 子类）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: BindException e 校验异常
     * @Return: ApiResult 失败返回
     */
    @ExceptionHandler(BindException.class)
    public ApiResult<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return ApiResult.fail("参数校验失败：" + msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        return ApiResult.fail("参数校验失败：" + msg);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class, MissingServletRequestPartException.class})
    public ApiResult<Void> handleBadRequest(Exception e) {
        log.warn("请求参数错误：{}", e.getMessage());
        return ApiResult.fail("请求参数格式不正确");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return ApiResult.fail("上传文件超过大小限制");
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Void> handleMethod(HttpRequestMethodNotSupportedException e) {
        return ApiResult.fail("不支持的请求方法");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return ApiResult.fail("系统繁忙，请稍后重试");
    }
}
