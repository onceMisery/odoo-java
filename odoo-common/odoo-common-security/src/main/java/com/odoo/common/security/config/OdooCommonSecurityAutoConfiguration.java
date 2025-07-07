package com.odoo.common.security.config;

import com.odoo.common.security.utils.JwtUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Odoo Common Security 自动配置类
 *
 * @author odoo
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.odoo.common.security")
public class OdooCommonSecurityAutoConfiguration {

    /**
     * JWT工具类
     */
    @Bean
    @ConditionalOnMissingBean(JwtUtils.class)
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }
} 