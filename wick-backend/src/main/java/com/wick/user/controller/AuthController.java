package com.wick.user.controller;

import com.wick.security.RefreshTokenService;
import com.wick.security.TokenPair;
import com.wick.user.domain.User;
import com.wick.user.dto.AuthResponse;
import com.wick.user.dto.EmailAvailabilityResponse;
import com.wick.user.dto.ForgotPasswordRequest;
import com.wick.user.dto.LoginRequest;
import com.wick.user.dto.RefreshTokenRequest;
import com.wick.user.dto.RegisterRequest;
import com.wick.user.dto.ResetPasswordRequest;
import com.wick.user.dto.TokenPairResponse;
import com.wick.user.mapper.UserMapper;
import com.wick.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/check-email")
    public EmailAvailabilityResponse checkEmail(@RequestParam @NotBlank @Email String email) {
        return new EmailAvailabilityResponse(userService.isEmailAvailable(email));
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        TokenPair tokens = refreshTokenService.issueTokenPair(user.getId().toString());
        return userMapper.toAuthResponse(user, tokens);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.email(), request.password());
        TokenPair tokens = refreshTokenService.issueTokenPair(user.getId().toString());
        return userMapper.toAuthResponse(user, tokens);
    }

    @PostMapping("/refresh")
    public TokenPairResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPair tokens = refreshTokenService.refresh(request.refreshToken());
        return new TokenPairResponse(tokens.accessToken(), tokens.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.email());
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
    }
}
