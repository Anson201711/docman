package com.docman.system.service.impl;

import com.docman.system.entity.OperationLog;
import com.docman.system.entity.StorageQuota;
import com.docman.system.entity.SystemConfig;
import com.docman.system.service.SystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final Map<Long, OperationLog> logStore = new ConcurrentHashMap<>();
    private final Map<String, SystemConfig> configStore = new ConcurrentHashMap<>();
    private final Map<Long, StorageQuota> quotaStore = new ConcurrentHashMap<>();
    private final AtomicLong logIdCounter = new AtomicLong(1);

    @Override
    public OperationLog createLog(OperationLog operationLog) {
        operationLog.setId(logIdCounter.getAndIncrement());
        operationLog.setCreateTime(LocalDateTime.now());
        logStore.put(operationLog.getId(), operationLog);
        log.info("Operation log created: {} {} {}", operationLog.getUserId(), operationLog.getModule(), operationLog.getOperation());
        return operationLog;
    }

    @Override
    public List<OperationLog> queryLogs(OperationLog query) {
        return logStore.values().stream()
                .filter(log -> query.getUserId() == null || query.getUserId().equals(log.getUserId()))
                .filter(log -> query.getModule() == null || query.getModule().equals(log.getModule()))
                .filter(log -> query.getOperation() == null || query.getOperation().equals(log.getOperation()))
                .collect(Collectors.toList());
    }

    @Override
    public OperationLog getLog(Long id) {
        return logStore.get(id);
    }

    @Override
    public SystemConfig createConfig(SystemConfig config) {
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        configStore.put(config.getConfigKey(), config);
        log.info("System config created: {}", config.getConfigKey());
        return config;
    }

    @Override
    public SystemConfig updateConfig(SystemConfig config) {
        config.setUpdateTime(LocalDateTime.now());
        configStore.put(config.getConfigKey(), config);
        log.info("System config updated: {}", config.getConfigKey());
        return config;
    }

    @Override
    public SystemConfig getConfig(String configKey) {
        return configStore.get(configKey);
    }

    @Override
    public List<SystemConfig> listConfigs() {
        return new ArrayList<>(configStore.values());
    }

    @Override
    public void deleteConfig(String configKey) {
        configStore.remove(configKey);
        log.info("System config deleted: {}", configKey);
    }

    @Override
    public StorageQuota createQuota(StorageQuota quota) {
        quota.setCreateTime(LocalDateTime.now());
        quota.setUpdateTime(LocalDateTime.now());
        quotaStore.put(quota.getUserId(), quota);
        log.info("Storage quota created for user {}", quota.getUserId());
        return quota;
    }

    @Override
    public StorageQuota updateQuota(StorageQuota quota) {
        quota.setUpdateTime(LocalDateTime.now());
        quotaStore.put(quota.getUserId(), quota);
        log.info("Storage quota updated for user {}", quota.getUserId());
        return quota;
    }

    @Override
    public StorageQuota getQuota(Long userId) {
        return quotaStore.get(userId);
    }

    @Override
    public List<StorageQuota> listQuotas() {
        return new ArrayList<>(quotaStore.values());
    }

    @Override
    public StorageQuota calculateUsage(Long userId) {
        StorageQuota quota = quotaStore.get(userId);
        if (quota == null) {
            quota = new StorageQuota();
            quota.setUserId(userId);
            quota.setTotalQuota(10737418240L);
            quota.setUsedQuota(0L);
        }
        return quota;
    }
}
