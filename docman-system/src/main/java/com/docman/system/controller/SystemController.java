package com.docman.system.controller;

import com.docman.system.entity.OperationLog;
import com.docman.system.entity.StorageQuota;
import com.docman.system.entity.SystemConfig;
import com.docman.system.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    @PostMapping("/log")
    public ResponseEntity<OperationLog> createLog(@RequestBody OperationLog log) {
        return ResponseEntity.ok(systemService.createLog(log));
    }

    @PostMapping("/logs/query")
    public ResponseEntity<List<OperationLog>> queryLogs(@RequestBody OperationLog query) {
        return ResponseEntity.ok(systemService.queryLogs(query));
    }

    @GetMapping("/log/{id}")
    public ResponseEntity<OperationLog> getLog(@PathVariable Long id) {
        return ResponseEntity.ok(systemService.getLog(id));
    }

    @PostMapping("/config")
    public ResponseEntity<SystemConfig> createConfig(@RequestBody SystemConfig config) {
        return ResponseEntity.ok(systemService.createConfig(config));
    }

    @PutMapping("/config")
    public ResponseEntity<SystemConfig> updateConfig(@RequestBody SystemConfig config) {
        return ResponseEntity.ok(systemService.updateConfig(config));
    }

    @GetMapping("/config/{configKey}")
    public ResponseEntity<SystemConfig> getConfig(@PathVariable String configKey) {
        return ResponseEntity.ok(systemService.getConfig(configKey));
    }

    @GetMapping("/configs")
    public ResponseEntity<List<SystemConfig>> listConfigs() {
        return ResponseEntity.ok(systemService.listConfigs());
    }

    @DeleteMapping("/config/{configKey}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String configKey) {
        systemService.deleteConfig(configKey);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quota")
    public ResponseEntity<StorageQuota> createQuota(@RequestBody StorageQuota quota) {
        return ResponseEntity.ok(systemService.createQuota(quota));
    }

    @PutMapping("/quota")
    public ResponseEntity<StorageQuota> updateQuota(@RequestBody StorageQuota quota) {
        return ResponseEntity.ok(systemService.updateQuota(quota));
    }

    @GetMapping("/quota/{userId}")
    public ResponseEntity<StorageQuota> getQuota(@PathVariable Long userId) {
        return ResponseEntity.ok(systemService.getQuota(userId));
    }

    @GetMapping("/quotas")
    public ResponseEntity<List<StorageQuota>> listQuotas() {
        return ResponseEntity.ok(systemService.listQuotas());
    }

    @GetMapping("/quota/{userId}/usage")
    public ResponseEntity<StorageQuota> calculateUsage(@PathVariable Long userId) {
        return ResponseEntity.ok(systemService.calculateUsage(userId));
    }
}
