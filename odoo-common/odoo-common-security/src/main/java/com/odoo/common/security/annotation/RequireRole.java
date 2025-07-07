package com.odoo.common.security.annotation;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * 用于方法级别的角色控制
 *
 * @author odoo
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 角色代码
     */
    String[] value() default {};

    /**
     * 角色代码（别名）
     */
    String[] roles() default {};

    /**
     * 逻辑关系
     * AND: 需要拥有所有角色
     * OR: 需要拥有任意一个角色
     */
    Logical logical() default Logical.AND;

    /**
     * 角色描述
     */
    String description() default "";

    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /**
         * 且关系（需要拥有所有角色）
         */
        AND,
        
        /**
         * 或关系（需要拥有任意一个角色）
         */
        OR
    }
} 