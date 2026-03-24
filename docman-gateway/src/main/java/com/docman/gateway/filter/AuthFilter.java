package com.docman.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication filter for JWT tokens
 */
@Slf4j
@Component
public class AuthFilter implements org.springframework.cloud.gateway.filter.GlobalFilter, Ordered {

    private static final String JWT_HEADER = "Authorization";
    private static final String JWT_PREFIX = "Bearer ";

    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            "docman-jwt-secret-key-2024-enterprise-edition-very-long-key".getBytes(StandardCharsets.UTF_8));

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/health",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(JWT_HEADER);
        if (authHeader == null || !authHeader.startsWith(JWT_PREFIX)) {
            return unauthorized(exchange.getResponse());
        }

        String token = authHeader.substring(JWT_PREFIX.length());

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Add user info to headers
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", claims.get("userId", String.class))
                    .header("X-Username", claims.getSubject())
                    .header("X-User-Role", claims.get("role", String.class))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return unauthorized(exchange.getResponse());
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"code\":401,\"message\":\"Unauthorized\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
