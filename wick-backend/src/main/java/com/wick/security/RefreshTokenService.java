package com.wick.security;

import com.wick.common.exception.ApiException;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenBlacklist refreshTokenBlacklist;
    private final UserService userService;

    public TokenPair issueTokenPair(String userId) {
        return new TokenPair(jwtService.generateAccessToken(userId), jwtService.generateRefreshToken(userId));
    }

    public TokenPair refresh(String refreshToken) {
        String jti = validateAndExtractJti(refreshToken);
        String userId = jwtService.extractSubject(refreshToken);
        if (userService.getById(UUID.fromString(userId)).getDeletedAt() != null) {
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        blacklist(refreshToken, jti);
        return issueTokenPair(userId);
    }

    public void revoke(String refreshToken) {
        String jti = validateAndExtractJti(refreshToken);
        blacklist(refreshToken, jti);
    }

    private String validateAndExtractJti(String refreshToken) {
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        String jti = jwtService.extractJti(refreshToken);
        if (refreshTokenBlacklist.isBlacklisted(jti)) {
            throw new ApiException("Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
        }
        return jti;
    }

    private void blacklist(String refreshToken, String jti) {
        Duration ttl = Duration.between(Instant.now(), jwtService.extractExpiration(refreshToken));
        refreshTokenBlacklist.blacklist(jti, ttl);
    }
}
