package com.docman.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration filter
 */
@Component
public class CorsFilter implements GlobalFilter, Ordered {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8080"
    );

    private static final List<String> ALLOWED_METHODS = Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    );

    private static final List<String> ALLOWED_HEADERS = Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With",
            "Accept", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String origin = request.getHeaders().getOrigin();

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.getHeaders().add("Access-Control-Allow-Origin", origin);
            response.getHeaders().add("Access-Control-Allow-Credentials", "true");
            response.getHeaders().add("Access-Control-Allow-Methods", String.join(", ", ALLOWED_METHODS));
            response.getHeaders().add("Access-Control-Allow-Headers", String.join(", ", ALLOWED_HEADERS));
            response.getHeaders().add("Access-Control-Max-Age", "3600");
        }

        if (request.getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            response.setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
