package com.docman.system.service;

import com.docman.system.entity.OperationLog;
import com.docman.system.entity.StorageQuota;
import com.docman.system.entity.SystemConfig;

import java.util.List;

public interface SystemService {
    OperationLog createLog(OperationLog log);
    List<OperationLog> queryLogs(OperationLog query);
    OperationLog getLog(Long id);

    SystemConfig createConfig(SystemConfig config);
    SystemConfig updateConfig(SystemConfig config);
    SystemConfig getConfig(String configKey);
    List<SystemConfig> listConfigs();
    void deleteConfig(String configKey);

    StorageQuota createQuota(StorageQuota quota);
    StorageQuota updateQuota(StorageQuota quota);
    StorageQuota getQuota(Long userId);
    List<StorageQuota> listQuotas();
    StorageQuota calculateUsage(Long userId);
}
