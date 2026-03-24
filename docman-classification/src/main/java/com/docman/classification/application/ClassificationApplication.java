package com.docman.classification.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.docman.classification", "com.docman.common"})
public class ClassificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassificationApplication.class, args);
    }
}
