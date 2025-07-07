package com.odoo.common.web.example;

import com.odoo.common.core.result.Result;
import com.odoo.common.security.annotation.RequirePermission;
import com.odoo.common.security.annotation.RequireRole;
import com.odoo.common.security.context.SecurityContextHolder;
import com.odoo.common.web.annotation.OperationLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 示例控制器
 * 展示各种注解的使用方法
 * 
 * 注意：这个控制器仅用于示例，可以通过配置禁用
 *
 * @author odoo
 */
@RestController
@RequestMapping("/example")
@ConditionalOnProperty(name = "odoo.example.enabled", havingValue = "true", matchIfMissing = false)
public class ExampleController {

    /**
     * 需要用户登录的接口
     */
    @GetMapping("/user-info")
    @OperationLog(value = "查看用户信息", module = "示例模块", type = OperationLog.OperationType.SELECT)
    public Result<Object> getUserInfo() {
        Long userId = SecurityContextHolder.getCurrentUserId();
        String username = SecurityContextHolder.getCurrentUsername();
        
        return Result.success("当前用户：" + username + " (ID: " + userId + ")");
    }

    /**
     * 需要管理员角色的接口
     */
    @GetMapping("/admin-only")
    @RequireRole("ADMIN")
    @OperationLog(value = "管理员专用操作", module = "示例模块", type = OperationLog.OperationType.OTHER)
    public Result<String> adminOnly() {
        return Result.success("管理员专用内容");
    }

    /**
     * 需要特定权限的接口
     */
    @GetMapping("/with-permission")
    @RequirePermission("user:read")
    @OperationLog(value = "权限控制操作", module = "示例模块", type = OperationLog.OperationType.SELECT)
    public Result<String> withPermission() {
        return Result.success("具有用户查看权限的内容");
    }

    /**
     * 需要多个权限的接口（OR逻辑）
     */
    @GetMapping("/with-any-permission")
    @RequirePermission(value = {"user:edit", "user:delete"}, logical = RequirePermission.Logical.OR)
    @OperationLog(value = "多权限操作", module = "示例模块", type = OperationLog.OperationType.OTHER)
    public Result<String> withAnyPermission() {
        return Result.success("具有用户编辑或删除权限的内容");
    }

    /**
     * 需要多个权限的接口（AND逻辑）
     */
    @GetMapping("/with-all-permissions")
    @RequirePermission(value = {"user:read", "user:edit"}, logical = RequirePermission.Logical.AND)
    @OperationLog(value = "全权限操作", module = "示例模块", type = OperationLog.OperationType.UPDATE)
    public Result<String> withAllPermissions() {
        return Result.success("同时具有用户查看和编辑权限的内容");
    }

    /**
     * 复杂的权限和角色组合
     */
    @GetMapping("/complex")
    @RequireRole(value = {"ADMIN", "MANAGER"}, logical = RequireRole.Logical.OR)
    @RequirePermission("system:config")
    @OperationLog(value = "复杂权限操作", module = "示例模块", type = OperationLog.OperationType.UPDATE, 
                  includeResult = true, async = false)
    public Result<String> complexPermission() {
        return Result.success("复杂权限验证通过");
    }

    /**
     * 公开接口，无需任何权限
     */
    @GetMapping("/public")
    @OperationLog(value = "公开接口访问", module = "示例模块", type = OperationLog.OperationType.SELECT)
    public Result<String> publicEndpoint() {
        return Result.success("这是公开接口，无需任何权限");
    }
} 