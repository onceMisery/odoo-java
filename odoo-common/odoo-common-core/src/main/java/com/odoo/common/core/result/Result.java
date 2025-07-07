package com.odoo.common.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一API响应结果封装
 *
 * @param <T> 数据类型
 * @author odoo
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "响应码")
    private String code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "响应时间")
    private LocalDateTime timestamp;

    @Schema(description = "请求跟踪ID")
    private String traceId;

    public Result() {
        this.timestamp = LocalDateTime.now();
    }

    public Result(Boolean success, String code, String message) {
        this();
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public Result(Boolean success, String code, String message, T data) {
        this(success, code, message);
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(false, ResultCode.INTERNAL_SERVER_ERROR.getCode(), 
                          ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(false, ResultCode.INTERNAL_SERVER_ERROR.getCode(), message);
    }

    /**
     * 失败响应（错误码和消息）
     */
    public static <T> Result<T> error(String code, String message) {
        return new Result<>(false, code, message);
    }

    /**
     * 失败响应（ResultCode枚举）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(false, resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败响应（ResultCode枚举和自定义消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(false, resultCode.getCode(), message);
    }

    /**
     * 根据布尔值返回结果
     */
    public static <T> Result<T> result(boolean flag) {
        return flag ? success() : error();
    }

    /**
     * 根据布尔值返回结果（带数据）
     */
    public static <T> Result<T> result(boolean flag, T data) {
        return flag ? success(data) : error();
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.success);
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 设置跟踪ID
     */
    public Result<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
} 