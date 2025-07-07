package com.odoo.common.core.config;

import com.odoo.common.core.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Odoo Common Core 自动配置类
 *
 * @author odoo
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.odoo.common.core")
public class OdooCommonCoreAutoConfiguration {

    /**
     * 全局异常处理器
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
} 