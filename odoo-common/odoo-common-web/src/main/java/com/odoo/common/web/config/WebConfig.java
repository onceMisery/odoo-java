package com.odoo.common.web.config;

import com.odoo.common.web.interceptor.SecurityContextInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 注册拦截器和其他Web相关配置
 *
 * @author odoo
 */
@Configuration
@ConditionalOnProperty(name = "odoo.security.interceptor.enabled", havingValue = "true", matchIfMissing = true)
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册安全上下文拦截器
        registry.addInterceptor(new SecurityContextInterceptor())
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                    "/actuator/**",     // 排除健康检查端点
                    "/error",           // 排除错误页面
                    "/favicon.ico",     // 排除网站图标
                    "/static/**",       // 排除静态资源
                    "/public/**"        // 排除公共资源
                );
    }
} 