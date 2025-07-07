package com.odoo.common.web.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于记录用户操作行为
 *
 * @author odoo
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作描述（别名）
     */
    String description() default "";

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 是否记录请求参数
     */
    boolean includeArgs() default true;

    /**
     * 是否记录返回结果
     */
    boolean includeResult() default false;

    /**
     * 是否异步记录日志
     */
    boolean async() default true;

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 查询
         */
        SELECT,

        /**
         * 新增
         */
        INSERT,

        /**
         * 修改
         */
        UPDATE,

        /**
         * 删除
         */
        DELETE,

        /**
         * 导入
         */
        IMPORT,

        /**
         * 导出
         */
        EXPORT,

        /**
         * 登录
         */
        LOGIN,

        /**
         * 登出
         */
        LOGOUT,

        /**
         * 其他
         */
        OTHER
    }
} 