package com.ucto.backend.service;

import com.ucto.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService token generation and validation.
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-jwt-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
                3600000L,
                604800000L
        );
    }

    @Test
    void generateAccessToken_ShouldProduceValidToken() {
        String token = jwtService.generateAccessToken(1L, "test@example.com", "FOUNDER");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT must have 3 parts");
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void generateRefreshToken_ShouldProduceValidToken() {
        String token = jwtService.generateRefreshToken(1L, "test@example.com");
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void parseToken_ShouldExtractClaims() {
        String token = jwtService.generateAccessToken(1L, "user@test.com", "DEVELOPER");
        Claims claims = jwtService.parseToken(token);
        assertEquals("user@test.com", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("DEVELOPER", claims.get("role", String.class));
    }

    @Test
    void getUserIdFromToken_ShouldReturnCorrectId() {
        String token = jwtService.generateAccessToken(42L, "id42@test.com", "VIEWER");
        assertEquals(42L, jwtService.getUserIdFromToken(token));
    }

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        String token = jwtService.generateAccessToken(1L, "email@test.com", "FOUNDER");
        assertEquals("email@test.com", jwtService.getEmailFromToken(token));
    }

    @Test
    void getRoleFromToken_ShouldReturnCorrectRole() {
        String token = jwtService.generateAccessToken(1L, "r@t.com", "UCTO_ADMIN");
        assertEquals("UCTO_ADMIN", jwtService.getRoleFromToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_WithTamperedToken_ShouldReturnFalse() {
        String token = jwtService.generateAccessToken(1L, "tamper@test.com", "FOUNDER");
        String tampered = token.substring(0, token.lastIndexOf('.')) + ".tampered";
        assertFalse(jwtService.validateToken(tampered));
    }

    @Test
    void validateToken_WithNull_ShouldReturnFalse() {
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void validateToken_WithEmptyString_ShouldReturnFalse() {
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    void accessAndRefreshTokens_Differ() {
        String access = jwtService.generateAccessToken(1L, "diff@test.com", "FOUNDER");
        String refresh = jwtService.generateRefreshToken(1L, "diff@test.com");
        assertNotEquals(access, refresh);
    }
}
