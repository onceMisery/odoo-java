package com.odoo.common.security.context;

/**
 * 安全上下文持有者
 * 使用ThreadLocal线程安全地存储当前用户信息
 *
 * @author odoo
 */
public class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户上下文
     */
    public static void setContext(SecurityContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前用户上下文
     */
    public static SecurityContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前用户上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        SecurityContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        SecurityContext context = getContext();
        return context != null ? context.getUsername() : null;
    }

    /**
     * 获取当前用户真实姓名
     */
    public static String getCurrentRealName() {
        SecurityContext context = getContext();
        return context != null ? context.getRealName() : null;
    }

    /**
     * 判断当前用户是否拥有指定角色
     */
    public static boolean hasRole(String roleCode) {
        SecurityContext context = getContext();
        return context != null && context.hasRole(roleCode);
    }

    /**
     * 判断当前用户是否拥有任意一个角色
     */
    public static boolean hasAnyRole(String... roleCodes) {
        SecurityContext context = getContext();
        return context != null && context.hasAnyRole(roleCodes);
    }

    /**
     * 判断当前用户是否拥有所有角色
     */
    public static boolean hasAllRoles(String... roleCodes) {
        SecurityContext context = getContext();
        return context != null && context.hasAllRoles(roleCodes);
    }

    /**
     * 判断当前用户是否拥有指定权限
     */
    public static boolean hasPermission(String permissionCode) {
        SecurityContext context = getContext();
        return context != null && context.hasPermission(permissionCode);
    }

    /**
     * 判断当前用户是否拥有任意一个权限
     */
    public static boolean hasAnyPermission(String... permissionCodes) {
        SecurityContext context = getContext();
        return context != null && context.hasAnyPermission(permissionCodes);
    }

    /**
     * 判断当前用户是否拥有所有权限
     */
    public static boolean hasAllPermissions(String... permissionCodes) {
        SecurityContext context = getContext();
        return context != null && context.hasAllPermissions(permissionCodes);
    }

    /**
     * 判断当前用户是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        SecurityContext context = getContext();
        return context != null && Boolean.TRUE.equals(context.getSuperAdmin());
    }

    /**
     * 判断是否已登录
     */
    public static boolean isAuthenticated() {
        SecurityContext context = getContext();
        return context != null && context.getUserId() != null;
    }
} 