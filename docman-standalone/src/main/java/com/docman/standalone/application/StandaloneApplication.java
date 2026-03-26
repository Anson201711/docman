package com.docman.standalone.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;

/**
 * DocMan Standalone Application
 * All-in-One JAR containing all services
 */
@SpringBootApplication(scanBasePackages = "com.docman",
        exclude = ReactiveManagementWebSecurityAutoConfiguration.class)
public class StandaloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(StandaloneApplication.class, args);
    }
}
