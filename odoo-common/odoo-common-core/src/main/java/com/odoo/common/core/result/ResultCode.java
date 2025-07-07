package com.odoo.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应码枚举
 *
 * @author odoo
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS("200", "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST("400", "请求参数错误"),
    UNAUTHORIZED("401", "未授权"),
    FORBIDDEN("403", "禁止访问"),
    NOT_FOUND("404", "资源不存在"),
    METHOD_NOT_ALLOWED("405", "请求方法不允许"),
    CONFLICT("409", "资源冲突"),
    VALIDATION_ERROR("422", "参数校验失败"),
    TOO_MANY_REQUESTS("429", "请求过于频繁"),

    // 服务器错误 5xx
    INTERNAL_SERVER_ERROR("500", "服务器内部错误"),
    SERVICE_UNAVAILABLE("503", "服务不可用"),
    GATEWAY_TIMEOUT("504", "网关超时"),

    // 业务错误 6xxx
    BUSINESS_ERROR("6000", "业务处理失败"),
    USER_NOT_FOUND("6001", "用户不存在"),
    USER_DISABLED("6002", "用户已禁用"),
    PASSWORD_ERROR("6003", "密码错误"),
    TOKEN_EXPIRED("6004", "Token已过期"),
    TOKEN_INVALID("6005", "Token无效"),
    PERMISSION_DENIED("6006", "权限不足"),
    DUPLICATE_KEY("6007", "数据已存在"),
    DATA_NOT_FOUND("6008", "数据不存在"),
    DATA_CONFLICT("6009", "数据冲突"),

    // CRM业务错误 61xx
    CUSTOMER_NOT_FOUND("6101", "客户不存在"),
    CUSTOMER_DISABLED("6102", "客户已禁用"),
    OPPORTUNITY_NOT_FOUND("6103", "销售机会不存在"),
    OPPORTUNITY_CLOSED("6104", "销售机会已关闭"),

    // 库存业务错误 62xx
    PRODUCT_NOT_FOUND("6201", "商品不存在"),
    PRODUCT_DISABLED("6202", "商品已禁用"),
    STOCK_INSUFFICIENT("6203", "库存不足"),
    WAREHOUSE_NOT_FOUND("6204", "仓库不存在"),

    // 财务业务错误 63xx
    ACCOUNT_NOT_FOUND("6301", "账户不存在"),
    INVOICE_NOT_FOUND("6302", "发票不存在"),
    PAYMENT_FAILED("6303", "支付失败"),
    AMOUNT_ERROR("6304", "金额错误"),

    // 系统错误 7xxx
    DATABASE_ERROR("7001", "数据库操作失败"),
    REDIS_ERROR("7002", "Redis操作失败"),
    MQ_ERROR("7003", "消息队列操作失败"),
    FILE_UPLOAD_ERROR("7004", "文件上传失败"),
    FILE_NOT_FOUND("7005", "文件不存在"),
    EMAIL_SEND_ERROR("7006", "邮件发送失败"),
    SMS_SEND_ERROR("7007", "短信发送失败");

    /**
     * 响应码
     */
    private final String code;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 根据code获取ResultCode
     */
    public static ResultCode getByCode(String code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }

    /**
     * 判断是否为成功码
     */
    public boolean isSuccess() {
        return SUCCESS.equals(this);
    }

    /**
     * 判断是否为错误码
     */
    public boolean isError() {
        return !isSuccess();
    }
} 