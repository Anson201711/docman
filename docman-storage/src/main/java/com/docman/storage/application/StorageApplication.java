package com.docman.storage.application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Storage Application Main Class
 * MinIO storage service for document management system
 */
@SpringBootApplication(scanBasePackages = {"com.docman.storage", "com.docman.common"})
@MapperScan("com.docman.storage.mapper")
public class StorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageApplication.class, args);
    }
}
