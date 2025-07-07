package com.odoo.common.core.constants;

/**
 * 通用常量
 *
 * @author odoo
 */
public interface CommonConstants {

    /**
     * 系统相关常量
     */
    interface System {
        /** 系统名称 */
        String APP_NAME = "odoo-java";
        
        /** 系统版本 */
        String APP_VERSION = "1.0.0";
        
        /** 系统编码 */
        String DEFAULT_CHARSET = "UTF-8";
        
        /** 系统默认时区 */
        String DEFAULT_TIMEZONE = "Asia/Shanghai";
        
        /** 系统默认语言 */
        String DEFAULT_LOCALE = "zh_CN";
    }

    /**
     * HTTP相关常量
     */
    interface Http {
        /** 请求头 - 授权 */
        String HEADER_AUTHORIZATION = "Authorization";
        
        /** 请求头 - Token */
        String HEADER_TOKEN = "X-Token";
        
        /** 请求头 - 用户代理 */
        String HEADER_USER_AGENT = "User-Agent";
        
        /** 请求头 - 内容类型 */
        String HEADER_CONTENT_TYPE = "Content-Type";
        
        /** 请求头 - 跟踪ID */
        String HEADER_TRACE_ID = "X-Trace-Id";
        
        /** JSON内容类型 */
        String CONTENT_TYPE_JSON = "application/json";
        
        /** 表单内容类型 */
        String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
        
        /** Token前缀 */
        String TOKEN_PREFIX = "Bearer ";
    }

    /**
     * 缓存相关常量
     */
    interface Cache {
        /** 默认缓存时间（秒） */
        long DEFAULT_TTL = 1800L; // 30分钟
        
        /** 短时间缓存（秒） */
        long SHORT_TTL = 300L;  // 5分钟
        
        /** 长时间缓存（秒） */
        long LONG_TTL = 7200L;  // 2小时
        
        /** 缓存键分隔符 */
        String KEY_SEPARATOR = ":";
        
        /** 用户缓存前缀 */
        String USER_PREFIX = "user";
        
        /** 权限缓存前缀 */
        String PERMISSION_PREFIX = "permission";
        
        /** 配置缓存前缀 */
        String CONFIG_PREFIX = "config";
    }

    /**
     * 数据状态常量
     */
    interface Status {
        /** 启用 */
        Integer ENABLED = 1;
        
        /** 禁用 */
        Integer DISABLED = 0;
        
        /** 删除标记 - 未删除 */
        Integer NOT_DELETED = 0;
        
        /** 删除标记 - 已删除 */
        Integer DELETED = 1;
    }

    /**
     * 分页常量
     */
    interface Page {
        /** 默认页码 */
        Integer DEFAULT_CURRENT = 1;
        
        /** 默认每页大小 */
        Integer DEFAULT_SIZE = 20;
        
        /** 最大每页大小 */
        Integer MAX_SIZE = 1000;
        
        /** 最小每页大小 */
        Integer MIN_SIZE = 1;
    }

    /**
     * 日期时间格式常量
     */
    interface DateTime {
        /** 日期格式 */
        String DATE_FORMAT = "yyyy-MM-dd";
        
        /** 时间格式 */
        String TIME_FORMAT = "HH:mm:ss";
        
        /** 日期时间格式 */
        String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        
        /** 时间戳格式 */
        String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
        
        /** ISO日期时间格式 */
        String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    }

    /**
     * 正则表达式常量
     */
    interface Regex {
        /** 手机号码 */
        String MOBILE = "^1[3-9]\\d{9}$";
        
        /** 邮箱 */
        String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        
        /** 身份证号 */
        String ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
        
        /** 用户名（字母数字下划线，3-20位） */
        String USERNAME = "^[a-zA-Z0-9_]{3,20}$";
        
        /** 密码（至少包含字母和数字，6-20位） */
        String PASSWORD = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{6,20}$";
        
        /** IP地址 */
        String IP = "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";
        
        /** URL */
        String URL = "^https?://(([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)*)([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?)$";
    }

    /**
     * 业务常量
     */
    interface Business {
        /** 默认用户头像 */
        String DEFAULT_AVATAR = "/static/images/default-avatar.png";
        
        /** 系统管理员角色代码 */
        String ADMIN_ROLE_CODE = "admin";
        
        /** 普通用户角色代码 */
        String USER_ROLE_CODE = "user";
        
        /** 超级管理员用户名 */
        String SUPER_ADMIN_USERNAME = "admin";
        
        /** 默认密码 */
        String DEFAULT_PASSWORD = "123456";
    }
} 