package com.petrochina.xnyqt.pims.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petrochina.xnyqt.pims.common.type.ResultCodeType;
import lombok.Data;

import java.io.Serializable;

/**
 * @Name: ApiResult
 * @Description: 接口统一返回结构 {code, msg, data}
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

    private String msg;

    private T data;

    /**
     * @Name: success
     * @Description: 构造成功返回
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: T data 业务数据
     * @Return: ApiResult 返回对象
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(ResultCodeType.SUCCESS.getCode());
        result.setMsg(ResultCodeType.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * @Name: fail
     * @Description: 构造失败返回
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: String msg 失败原因
     * @Return: ApiResult 返回对象
     */
    public static <T> ApiResult<T> fail(String msg) {
        return of(ResultCodeType.FAIL, msg);
    }

    /**
     * @Name: of
     * @Description: 按返回码类型构造返回
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: ResultCodeType type 返回码, String msg 提示信息
     * @Return: ApiResult 返回对象
     */
    public static <T> ApiResult<T> of(ResultCodeType type, String msg) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(type.getCode());
        result.setMsg(msg == null ? type.getMessage() : msg);
        return result;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return ResultCodeType.SUCCESS.getCode().equals(code);
    }
}
