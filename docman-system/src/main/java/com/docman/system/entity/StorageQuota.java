package com.docman.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StorageQuota {
    private Long id;
    private Long userId;
    private String userName;
    private Long totalQuota;
    private Long usedQuota;
    private Long fileCount;
    private Long warningThreshold;
    private String quotaType;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
