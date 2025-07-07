package com.odoo.common.web.domain;

import com.odoo.common.web.annotation.OperationLog;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志记录
 *
 * @author odoo
 */
@Data
public class OperationLogRecord {

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private OperationLog.OperationType type;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 返回结果
     */
    private String result;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间（毫秒）
     */
    private Long executeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 