package com.docman.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String configName;
    private String configType;
    private String description;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
