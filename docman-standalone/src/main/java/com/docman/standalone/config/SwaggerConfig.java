package com.docman.standalone.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger Configuration for Standalone Mode
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI docManOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DocMan API")
                        .description("DocMan Document Management System - Standalone Mode API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DocMan Team")
                                .url("https://github.com/Anson201711/docman")));
    }

    @Bean
    public GroupedOpenApi allOpenApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/**")
                .build();
    }
}
