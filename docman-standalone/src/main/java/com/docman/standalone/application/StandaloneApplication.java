package com.docman.standalone.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DocMan Standalone Application
 * All-in-One JAR containing all services
 */
@SpringBootApplication(scanBasePackages = "com.docman")
public class StandaloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(StandaloneApplication.class, args);
    }
}
