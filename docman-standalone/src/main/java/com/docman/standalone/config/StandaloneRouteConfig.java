package com.docman.standalone.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Standalone Mode Route Configuration
 *
 * In standalone mode, all services run in a single JVM.
 * Gateway uses 'forward:' to route requests directly to local controllers
 * instead of making HTTP calls to other services.
 *
 * This avoids network overhead and eliminates the need for service discovery.
 */
@Configuration
public class StandaloneRouteConfig {

    @Bean
    public RouteLocator standaloneRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service - Auth endpoints (public, no auth required)
                .route("auth-login", r -> r
                        .path("/api/auth/login")
                        .filters(f -> f.stripPrefix(0))
                        .uri("forward:/auth/login"))

                .route("auth-register", r -> r
                        .path("/api/auth/register")
                        .filters(f -> f.stripPrefix(0))
                        .uri("forward:/auth/register"))

                .route("auth-refresh", r -> r
                        .path("/api/auth/refresh")
                        .filters(f -> f.stripPrefix(0))
                        .uri("forward:/auth/refresh"))

                // User Service - User/Group endpoints
                .route("user-service", r -> r
                        .path("/api/users/**", "/api/groups/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Document Service
                .route("document-service", r -> r
                        .path("/api/documents/**", "/api/folders/**", "/api/trash/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Storage Service
                .route("storage-service", r -> r
                        .path("/api/storage/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Search Service
                .route("search-service", r -> r
                        .path("/api/search/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Version Service
                .route("version-service", r -> r
                        .path("/api/versions/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Classification Service
                .route("classification-service", r -> r
                        .path("/api/categories/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Approval Service
                .route("approval-service", r -> r
                        .path("/api/approvals/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Subscription Service
                .route("subscription-service", r -> r
                        .path("/api/subscriptions/**", "/api/notifications/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Collaboration Service
                .route("collaboration-service", r -> r
                        .path("/api/collaborations/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // CAD Service
                .route("cad-service", r -> r
                        .path("/api/cad/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // System Service
                .route("system-service", r -> r
                        .path("/api/system/**", "/api/logs/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("forward:/"))

                // Health check
                .route("health", r -> r
                        .path("/actuator/**")
                        .uri("forward:/"))

                .build();
    }
}
