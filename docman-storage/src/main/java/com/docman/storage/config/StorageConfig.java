package com.docman.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Storage Configuration
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageConfig {

    private Long defaultQuota;
    private Long maxFileSize;
    private List<String> allowedExtensions;
}
