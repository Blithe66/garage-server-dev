package com.yixin.garage.service.sys;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yixin.garage.entity.OperationLog;

/**
 * <p>
 * 操作日志 服务类
 * </p>
 *
 */
public interface IOperationLogService extends IService<OperationLog> {

    /**
     * 获取操作日志列表
     */
    List<Map<String, Object>> getOperationLogs(Page<OperationLog> page, String beginTime, String endTime, String logName, String s, String orderByField, boolean asc);
}
