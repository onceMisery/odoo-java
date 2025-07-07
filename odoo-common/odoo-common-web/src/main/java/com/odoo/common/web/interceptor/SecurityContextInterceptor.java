package com.odoo.common.web.interceptor;

import cn.hutool.core.util.StrUtil;
import com.odoo.common.security.context.SecurityContext;
import com.odoo.common.security.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 安全上下文拦截器
 * 从网关传递的请求头中获取用户信息，设置到当前线程的安全上下文中
 *
 * @author odoo
 */
@Slf4j
public class SecurityContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 从请求头中获取用户信息（由网关设置）
            String userIdStr = request.getHeader("X-User-Id");
            String username = request.getHeader("X-Username");
            String realName = request.getHeader("X-Real-Name");
            String roleCode = request.getHeader("X-Role-Code");

            // 如果有用户信息，创建安全上下文
            if (StrUtil.isNotBlank(userIdStr) && StrUtil.isNotBlank(username)) {
                SecurityContext context = new SecurityContext();
                
                try {
                    context.setUserId(Long.parseLong(userIdStr));
                } catch (NumberFormatException e) {
                    log.warn("用户ID格式错误：{}", userIdStr);
                    return true;
                }
                
                context.setUsername(username);
                context.setRealName(realName);
                context.setLoginTime(LocalDateTime.now());
                context.setLoginIp(getClientIp(request));
                context.setUserAgent(request.getHeader("User-Agent"));

                // 设置角色（这里简化处理，实际项目中可能需要查询数据库）
                Set<String> roleCodes = new HashSet<>();
                if (StrUtil.isNotBlank(roleCode)) {
                    roleCodes.add(roleCode);
                }
                context.setRoleCodes(roleCodes);

                // 设置权限（这里简化处理，实际项目中需要根据角色查询权限）
                Set<String> permissionCodes = new HashSet<>();
                // 可以根据角色代码查询对应的权限
                context.setPermissionCodes(permissionCodes);

                // 检查是否为超级管理员
                context.setSuperAdmin("SUPER_ADMIN".equals(roleCode));

                // 设置到线程本地存储
                SecurityContextHolder.setContext(context);
                
                log.debug("设置安全上下文成功：用户 {} (ID: {})", username, userIdStr);
            }
        } catch (Exception e) {
            log.error("设置安全上下文失败", e);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 请求处理完成后的逻辑
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除安全上下文，避免内存泄漏
        try {
            SecurityContextHolder.clearContext();
            log.debug("清除安全上下文成功");
        } catch (Exception e) {
            log.error("清除安全上下文失败", e);
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
} 