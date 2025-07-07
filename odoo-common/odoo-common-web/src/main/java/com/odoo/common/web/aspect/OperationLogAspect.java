package com.odoo.common.web.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.odoo.common.security.context.SecurityContextHolder;
import com.odoo.common.web.annotation.OperationLog;
import com.odoo.common.web.domain.OperationLogRecord;
import com.odoo.common.web.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面
 * 记录用户操作行为
 *
 * @author odoo
 */
@Slf4j
@Aspect
@Component
@Order(2) // 在安全校验之后执行
public class OperationLogAspect {

    @Autowired(required = false)
    private OperationLogService operationLogService;

    /**
     * 环绕通知记录操作日志
     */
    @Around("@annotation(com.odoo.common.web.annotation.OperationLog)")
    public Object recordOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        if (operationLog == null) {
            return joinPoint.proceed();
        }

        // 构建日志记录对象
        OperationLogRecord logRecord = buildLogRecord(joinPoint, operationLog);

        Object result = null;
        Exception exception = null;
        long startTime = System.currentTimeMillis();

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            logRecord.setSuccess(true);
            logRecord.setErrorMessage(null);

            // 记录返回结果
            if (operationLog.includeResult() && result != null) {
                try {
                    String resultJson = JSONUtil.toJsonStr(result);
                    // 限制结果长度，避免日志过大
                    if (resultJson.length() > 2000) {
                        resultJson = resultJson.substring(0, 2000) + "...";
                    }
                    logRecord.setResult(resultJson);
                } catch (Exception e) {
                    log.warn("序列化返回结果失败：{}", e.getMessage());
                    logRecord.setResult("序列化失败");
                }
            }

        } catch (Exception e) {
            exception = e;
            logRecord.setSuccess(false);
            logRecord.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            // 计算执行时间
            long endTime = System.currentTimeMillis();
            logRecord.setExecuteTime(endTime - startTime);

            // 记录日志
            if (operationLog.async()) {
                // 异步记录日志
                recordLogAsync(logRecord);
            } else {
                // 同步记录日志
                recordLogSync(logRecord);
            }
        }

        return result;
    }

    /**
     * 构建日志记录对象
     */
    private OperationLogRecord buildLogRecord(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
        OperationLogRecord logRecord = new OperationLogRecord();

        // 基本信息
        logRecord.setOperateTime(LocalDateTime.now());
        logRecord.setModule(operationLog.module());
        logRecord.setType(operationLog.type());
        
        // 操作描述
        String description = StrUtil.isNotBlank(operationLog.description()) 
            ? operationLog.description() 
            : operationLog.value();
        logRecord.setDescription(description);

        // 方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        logRecord.setClassName(signature.getDeclaringTypeName());
        logRecord.setMethodName(signature.getName());

        // 用户信息
        logRecord.setUserId(SecurityContextHolder.getCurrentUserId());
        logRecord.setUsername(SecurityContextHolder.getCurrentUsername());
        logRecord.setRealName(SecurityContextHolder.getCurrentRealName());

        // 请求信息
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logRecord.setRequestUri(request.getRequestURI());
            logRecord.setRequestMethod(request.getMethod());
            logRecord.setUserAgent(request.getHeader("User-Agent"));
            logRecord.setClientIp(getClientIp(request));
        }

        // 请求参数
        if (operationLog.includeArgs()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    // 过滤敏感参数
                    Object[] filteredArgs = filterSensitiveArgs(args);
                    String argsJson = JSONUtil.toJsonStr(filteredArgs);
                    // 限制参数长度
                    if (argsJson.length() > 2000) {
                        argsJson = argsJson.substring(0, 2000) + "...";
                    }
                    logRecord.setRequestParams(argsJson);
                } catch (Exception e) {
                    log.warn("序列化请求参数失败：{}", e.getMessage());
                    logRecord.setRequestParams("序列化失败");
                }
            }
        }

        return logRecord;
    }

    /**
     * 过滤敏感参数
     */
    private Object[] filterSensitiveArgs(Object[] args) {
        return Arrays.stream(args)
            .map(arg -> {
                if (arg == null) {
                    return null;
                }
                String argStr = arg.toString();
                // 简单的敏感信息过滤，实际项目中应该更严格
                if (argStr.contains("password") || argStr.contains("token")) {
                    return "***";
                }
                return arg;
            })
            .toArray();
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

    /**
     * 异步记录日志
     */
    private void recordLogAsync(OperationLogRecord logRecord) {
        try {
            // 使用线程池异步处理
            if (operationLogService != null) {
                operationLogService.saveLogAsync(logRecord);
            } else {
                // 如果没有注入服务，直接打印日志
                log.info("操作日志：{}", JSONUtil.toJsonStr(logRecord));
            }
        } catch (Exception e) {
            log.error("异步记录操作日志失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 同步记录日志
     */
    private void recordLogSync(OperationLogRecord logRecord) {
        try {
            if (operationLogService != null) {
                operationLogService.saveLog(logRecord);
            } else {
                // 如果没有注入服务，直接打印日志
                log.info("操作日志：{}", JSONUtil.toJsonStr(logRecord));
            }
        } catch (Exception e) {
            log.error("同步记录操作日志失败：{}", e.getMessage(), e);
        }
    }
} 