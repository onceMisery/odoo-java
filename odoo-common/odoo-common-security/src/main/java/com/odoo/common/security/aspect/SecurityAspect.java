package com.odoo.common.security.aspect;

import com.odoo.common.core.exception.BusinessException;
import com.odoo.common.core.result.ResultCode;
import com.odoo.common.security.annotation.RequirePermission;
import com.odoo.common.security.annotation.RequireRole;
import com.odoo.common.security.context.SecurityContext;
import com.odoo.common.security.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 安全校验切面
 * 处理权限和角色验证
 *
 * @author odoo
 */
@Slf4j
@Aspect
@Component
@Order(1) // 确保在其他切面之前执行
public class SecurityAspect {

    /**
     * 权限校验
     */
    @Before("@annotation(com.odoo.common.security.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);

        if (requirePermission == null) {
            return;
        }

        // 获取当前用户上下文
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            log.warn("权限校验失败：用户未登录，方法：{}", method.getName());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        // 超级管理员直接通过
        if (Boolean.TRUE.equals(context.getSuperAdmin())) {
            log.debug("超级管理员访问，跳过权限校验，用户：{}", context.getUsername());
            return;
        }

        // 获取需要的权限
        String[] permissions = requirePermission.value();
        if (permissions.length == 0) {
            permissions = requirePermission.permissions();
        }

        if (permissions.length == 0) {
            log.warn("权限注解配置错误：未指定权限代码，方法：{}", method.getName());
            return;
        }

        // 检查权限
        boolean hasPermission = checkUserPermissions(context, permissions, requirePermission.logical());
        
        if (!hasPermission) {
            log.warn("权限校验失败：用户 {} 缺少权限 {}，方法：{}", 
                    context.getUsername(), String.join(",", permissions), method.getName());
            throw new BusinessException(ResultCode.FORBIDDEN, 
                    "权限不足：" + requirePermission.description());
        }

        log.debug("权限校验通过：用户 {} 访问方法 {}", context.getUsername(), method.getName());
    }

    /**
     * 角色校验
     */
    @Before("@annotation(com.odoo.common.security.annotation.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        if (requireRole == null) {
            return;
        }

        // 获取当前用户上下文
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            log.warn("角色校验失败：用户未登录，方法：{}", method.getName());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        // 超级管理员直接通过
        if (Boolean.TRUE.equals(context.getSuperAdmin())) {
            log.debug("超级管理员访问，跳过角色校验，用户：{}", context.getUsername());
            return;
        }

        // 获取需要的角色
        String[] roles = requireRole.value();
        if (roles.length == 0) {
            roles = requireRole.roles();
        }

        if (roles.length == 0) {
            log.warn("角色注解配置错误：未指定角色代码，方法：{}", method.getName());
            return;
        }

        // 检查角色
        boolean hasRole = checkUserRoles(context, roles, requireRole.logical());
        
        if (!hasRole) {
            log.warn("角色校验失败：用户 {} 缺少角色 {}，方法：{}", 
                    context.getUsername(), String.join(",", roles), method.getName());
            throw new BusinessException(ResultCode.FORBIDDEN, 
                    "角色权限不足：" + requireRole.description());
        }

        log.debug("角色校验通过：用户 {} 访问方法 {}", context.getUsername(), method.getName());
    }

    /**
     * 检查用户权限
     */
    private boolean checkUserPermissions(SecurityContext context, String[] permissions, 
                                       RequirePermission.Logical logical) {
        if (logical == RequirePermission.Logical.AND) {
            // AND 逻辑：需要拥有所有权限
            return context.hasAllPermissions(permissions);
        } else {
            // OR 逻辑：需要拥有任意一个权限
            return context.hasAnyPermission(permissions);
        }
    }

    /**
     * 检查用户角色
     */
    private boolean checkUserRoles(SecurityContext context, String[] roles, 
                                 RequireRole.Logical logical) {
        if (logical == RequireRole.Logical.AND) {
            // AND 逻辑：需要拥有所有角色
            return context.hasAllRoles(roles);
        } else {
            // OR 逻辑：需要拥有任意一个角色
            return context.hasAnyRole(roles);
        }
    }
} 