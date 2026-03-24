package com.docman.version.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.docman.version", "com.docman.common"})
public class VersionApplication {

    public static void main(String[] args) {
        SpringApplication.run(VersionApplication.class, args);
    }
}
