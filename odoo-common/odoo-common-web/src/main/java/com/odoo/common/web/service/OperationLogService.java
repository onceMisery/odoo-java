package com.odoo.common.web.service;

import com.odoo.common.web.domain.OperationLogRecord;

/**
 * 操作日志服务接口
 * 具体的实现类应该在各个业务模块中提供
 *
 * @author odoo
 */
public interface OperationLogService {

    /**
     * 同步保存操作日志
     */
    void saveLog(OperationLogRecord logRecord);

    /**
     * 异步保存操作日志
     */
    void saveLogAsync(OperationLogRecord logRecord);
} 