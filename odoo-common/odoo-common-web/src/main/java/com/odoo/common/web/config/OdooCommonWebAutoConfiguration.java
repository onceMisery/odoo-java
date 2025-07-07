package com.odoo.common.web.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Odoo Common Web 自动配置类
 *
 * @author odoo
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.odoo.common.web")
@Import({
    CorsConfig.class,
    JacksonConfig.class,
    WebConfig.class,
    AsyncConfig.class
})
public class OdooCommonWebAutoConfiguration {
    
} 