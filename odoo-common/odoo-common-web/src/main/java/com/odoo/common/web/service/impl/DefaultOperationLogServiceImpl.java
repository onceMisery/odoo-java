package com.odoo.common.web.service.impl;

import cn.hutool.json.JSONUtil;
import com.odoo.common.web.domain.OperationLogRecord;
import com.odoo.common.web.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 默认操作日志服务实现
 * 使用日志输出的方式记录操作日志
 * 具体项目可以实现自己的 OperationLogService 来替换这个默认实现
 *
 * @author odoo
 */
@Slf4j
@Service
@ConditionalOnMissingBean(OperationLogService.class)
public class DefaultOperationLogServiceImpl implements OperationLogService {

    @Override
    public void saveLog(OperationLogRecord logRecord) {
        try {
            // 同步记录到日志文件
            log.info("=== 操作日志 ===");
            log.info("用户: {} ({})", logRecord.getUsername(), logRecord.getUserId());
            log.info("操作: {} - {}", logRecord.getModule(), logRecord.getDescription());
            log.info("方法: {}.{}", logRecord.getClassName(), logRecord.getMethodName());
            log.info("请求: {} {}", logRecord.getRequestMethod(), logRecord.getRequestUri());
            log.info("IP: {}", logRecord.getClientIp());
            log.info("时间: {}ms", logRecord.getExecuteTime());
            log.info("结果: {}", logRecord.getSuccess() ? "成功" : "失败");
            
            if (!logRecord.getSuccess() && logRecord.getErrorMessage() != null) {
                log.info("错误: {}", logRecord.getErrorMessage());
            }
            
            log.debug("详细信息: {}", JSONUtil.toJsonStr(logRecord));
            log.info("=== 操作日志结束 ===");
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    @Override
    @Async
    public void saveLogAsync(OperationLogRecord logRecord) {
        // 异步调用同步方法
        saveLog(logRecord);
    }
} 