package com.wick.security;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenBlacklist refreshTokenBlacklist;

    @Mock
    private UserService userService;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(jwtService, refreshTokenBlacklist, userService);
    }

    private User activeUser(UUID id) {
        User user = TestEntities.user("user@example.com", "hash", "User");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void issueTokenPair_returnsFreshAccessAndRefreshTokens() {
        when(jwtService.generateAccessToken("user-1")).thenReturn("access-token");
        when(jwtService.generateRefreshToken("user-1")).thenReturn("refresh-token");

        TokenPair tokens = refreshTokenService.issueTokenPair("user-1");

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void refresh_blacklistsOldTokenAndIssuesNewPair_whenRefreshTokenIsValid() {
        UUID userId = UUID.randomUUID();
        String oldRefreshToken = "old-refresh-token";
        when(jwtService.isValid(oldRefreshToken)).thenReturn(true);
        when(jwtService.isRefreshToken(oldRefreshToken)).thenReturn(true);
        when(jwtService.extractJti(oldRefreshToken)).thenReturn("jti-1");
        when(refreshTokenBlacklist.isBlacklisted("jti-1")).thenReturn(false);
        when(jwtService.extractSubject(oldRefreshToken)).thenReturn(userId.toString());
        when(userService.getById(userId)).thenReturn(activeUser(userId));
        when(jwtService.extractExpiration(oldRefreshToken)).thenReturn(Instant.now().plusSeconds(60));
        when(jwtService.generateAccessToken(userId.toString())).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(userId.toString())).thenReturn("new-refresh-token");

        TokenPair tokens = refreshTokenService.refresh(oldRefreshToken);

        assertThat(tokens.accessToken()).isEqualTo("new-access-token");
        assertThat(tokens.refreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenBlacklist).blacklist(eq("jti-1"), any(Duration.class));
    }

    @Test
    void refresh_throws_whenUserAccountWasDeleted() {
        UUID userId = UUID.randomUUID();
        String refreshToken = "refresh-token";
        when(jwtService.isValid(refreshToken)).thenReturn(true);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractJti(refreshToken)).thenReturn("jti-deleted");
        when(refreshTokenBlacklist.isBlacklisted("jti-deleted")).thenReturn(false);
        when(jwtService.extractSubject(refreshToken)).thenReturn(userId.toString());
        User deletedUser = activeUser(userId);
        ReflectionTestUtils.setField(deletedUser, "deletedAt", Instant.now());
        when(userService.getById(userId)).thenReturn(deletedUser);

        assertThatThrownBy(() -> refreshTokenService.refresh(refreshToken))
                .isInstanceOf(ApiException.class);

        verify(refreshTokenBlacklist, never()).blacklist(any(), any());
    }

    @Test
    void refresh_throws_whenTokenIsNotARefreshToken() {
        when(jwtService.isValid("access-token")).thenReturn(true);
        when(jwtService.isRefreshToken("access-token")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.refresh("access-token"))
                .isInstanceOf(ApiException.class);

        verify(refreshTokenBlacklist, never()).blacklist(any(), any());
    }

    @Test
    void refresh_throws_whenSignatureInvalidOrExpired() {
        when(jwtService.isValid("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.refresh("bad-token"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void refresh_throws_whenTokenAlreadyBlacklisted() {
        when(jwtService.isValid("reused-token")).thenReturn(true);
        when(jwtService.isRefreshToken("reused-token")).thenReturn(true);
        when(jwtService.extractJti("reused-token")).thenReturn("jti-2");
        when(refreshTokenBlacklist.isBlacklisted("jti-2")).thenReturn(true);

        assertThatThrownBy(() -> refreshTokenService.refresh("reused-token"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void revoke_blacklistsToken_whenValid() {
        String refreshToken = "refresh-token";
        when(jwtService.isValid(refreshToken)).thenReturn(true);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractJti(refreshToken)).thenReturn("jti-3");
        when(refreshTokenBlacklist.isBlacklisted("jti-3")).thenReturn(false);
        when(jwtService.extractExpiration(refreshToken)).thenReturn(Instant.now().plusSeconds(120));

        refreshTokenService.revoke(refreshToken);

        verify(refreshTokenBlacklist).blacklist(eq("jti-3"), any(Duration.class));
    }
}
