package com.odoo.common.security.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于方法级别的权限控制
 *
 * @author odoo
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限代码
     */
    String[] value() default {};

    /**
     * 权限代码（别名）
     */
    String[] permissions() default {};

    /**
     * 逻辑关系
     * AND: 需要拥有所有权限
     * OR: 需要拥有任意一个权限
     */
    Logical logical() default Logical.AND;

    /**
     * 权限描述
     */
    String description() default "";

    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /**
         * 且关系（需要拥有所有权限）
         */
        AND,
        
        /**
         * 或关系（需要拥有任意一个权限）
         */
        OR
    }
} 