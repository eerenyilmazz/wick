package com.wick.security;

import com.wick.security.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha");
        jwtProperties.setAccessTokenExpirationMs(900_000);
        jwtProperties.setRefreshTokenExpirationMs(2_592_000_000L);
        jwtService = new JwtService(jwtProperties);
        jwtService.init();
    }

    @Test
    void generateAccessToken_isValidAndTypedAsAccess() {
        String subject = UUID.randomUUID().toString();
        String token = jwtService.generateAccessToken(subject);

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.isAccessToken(token)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isFalse();
        assertThat(jwtService.extractSubject(token)).isEqualTo(subject);
    }

    @Test
    void generateRefreshToken_isValidAndTypedAsRefresh_withUniqueJti() {
        String subject = UUID.randomUUID().toString();
        String token1 = jwtService.generateRefreshToken(subject);
        String token2 = jwtService.generateRefreshToken(subject);

        assertThat(jwtService.isRefreshToken(token1)).isTrue();
        assertThat(jwtService.isAccessToken(token1)).isFalse();
        assertThat(jwtService.extractJti(token1)).isNotBlank();
        assertThat(jwtService.extractJti(token1)).isNotEqualTo(jwtService.extractJti(token2));
    }

    @Test
    void isValid_returnsFalse_forGarbageToken() {
        assertThat(jwtService.isValid("not-a-real-token")).isFalse();
    }

    @Test
    void extractExpiration_isInTheFuture() {
        String token = jwtService.generateRefreshToken(UUID.randomUUID().toString());

        assertThat(jwtService.extractExpiration(token)).isAfter(java.time.Instant.now());
    }
}
