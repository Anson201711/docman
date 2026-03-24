package com.docman.document.application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.docman.document", "com.docman.common"})
@MapperScan("com.docman.document.repository")
@EnableCaching
@EnableAsync
public class DocumentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentApplication.class, args);
    }
}
