package com.odoo.common.security.context;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 安全上下文 - 当前登录用户信息
 *
 * @author odoo
 */
@Data
public class SecurityContext implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 角色代码集合
     */
    private Set<String> roleCodes;

    /**
     * 权限代码集合
     */
    private Set<String> permissionCodes;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * Token
     */
    private String token;

    /**
     * 是否为超级管理员
     */
    private Boolean superAdmin;

    /**
     * 判断是否拥有指定角色
     */
    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }

    /**
     * 判断是否拥有任意一个角色
     */
    public boolean hasAnyRole(String... roleCodes) {
        if (this.roleCodes == null || roleCodes == null) {
            return false;
        }
        for (String roleCode : roleCodes) {
            if (this.roleCodes.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否拥有所有角色
     */
    public boolean hasAllRoles(String... roleCodes) {
        if (this.roleCodes == null || roleCodes == null) {
            return false;
        }
        for (String roleCode : roleCodes) {
            if (!this.roleCodes.contains(roleCode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否拥有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        return permissionCodes != null && permissionCodes.contains(permissionCode);
    }

    /**
     * 判断是否拥有任意一个权限
     */
    public boolean hasAnyPermission(String... permissionCodes) {
        if (this.permissionCodes == null || permissionCodes == null) {
            return false;
        }
        for (String permissionCode : permissionCodes) {
            if (this.permissionCodes.contains(permissionCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否拥有所有权限
     */
    public boolean hasAllPermissions(String... permissionCodes) {
        if (this.permissionCodes == null || permissionCodes == null) {
            return false;
        }
        for (String permissionCode : permissionCodes) {
            if (!this.permissionCodes.contains(permissionCode)) {
                return false;
            }
        }
        return true;
    }
} 