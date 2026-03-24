package com.docman.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT utility tests
 */
class JwtUtilTest {

    @Test
    void generateToken_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";

        // When
        String token = JwtUtil.generateToken(userId, username, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = JwtUtil.generateToken(userId, username, role);

        // When
        Claims claims = JwtUtil.parseToken(token);

        // Then
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals(role, claims.get("role", String.class));
    }

    @Test
    void getUserId_Success() {
        // Given
        Long userId = 123L;
        String token = JwtUtil.generateToken(userId, "testuser", "USER");

        // When
        Long extractedUserId = JwtUtil.getUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void getUsername_Success() {
        // Given
        String username = "testuser";
        String token = JwtUtil.generateToken(1L, username, "USER");

        // When
        String extractedUsername = JwtUtil.getUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void getRole_Success() {
        // Given
        String role = "ADMIN";
        String token = JwtUtil.generateToken(1L, "testuser", role);

        // When
        String extractedRole = JwtUtil.getRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Given
        String token = JwtUtil.generateToken(1L, "testuser", "USER");

        // When
        boolean isValid = JwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = JwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_NonExpiredToken_ReturnsFalse() {
        // Given
        String token = JwtUtil.generateToken(1L, "testuser", "USER", 15 * 60 * 1000); // 15 minutes

        // When
        boolean isExpired = JwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        // Given
        String token = JwtUtil.generateToken(1L, "testuser", "USER", -1000); // Already expired

        // When/Then
        assertThrows(ExpiredJwtException.class, () -> JwtUtil.isTokenExpired(token));
    }

    @Test
    void generateRefreshToken_Success() {
        // Given
        Long userId = 1L;

        // When
        String refreshToken = JwtUtil.generateRefreshToken(userId);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }
}
