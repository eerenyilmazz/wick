package com.wick.user.controller;

import com.wick.photo.service.UserPhotoService;
import com.wick.token.service.TokenService;
import com.wick.user.dto.DataExportResponse;
import com.wick.user.dto.DeleteAccountRequest;
import com.wick.user.dto.DiscoveryPreferenceResponse;
import com.wick.user.dto.UpdateDeviceTokenRequest;
import com.wick.user.dto.UpdateDiscoveryPreferenceRequest;
import com.wick.user.dto.UpdateProfileRequest;
import com.wick.user.dto.UserResponse;
import com.wick.user.mapper.UserMapper;
import com.wick.user.service.DataExportService;
import com.wick.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final UserPhotoService userPhotoService;
    private final DataExportService dataExportService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal String userId) {
        return userMapper.toResponse(userService.getById(UUID.fromString(userId)));
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(@AuthenticationPrincipal String userId,
                                           @Valid @RequestBody UpdateProfileRequest request) {
        return userMapper.toResponse(userService.updateProfile(UUID.fromString(userId), request));
    }

    @PostMapping("/me/verify")
    public UserResponse verifyCurrentUser(@AuthenticationPrincipal String userId) {
        UUID id = UUID.fromString(userId);
        UserResponse response = userMapper.toResponse(userService.verify(id));
        tokenService.grantVerificationBonusIfEligible(id);
        return response;
    }

    @PutMapping("/me/device-token")
    public UserResponse updateDeviceToken(@AuthenticationPrincipal String userId,
                                           @Valid @RequestBody UpdateDeviceTokenRequest request) {
        return userMapper.toResponse(userService.updateDeviceToken(UUID.fromString(userId), request.fcmToken()));
    }

    @DeleteMapping("/me")
    public void deleteCurrentUser(@AuthenticationPrincipal String userId,
                                   @Valid @RequestBody DeleteAccountRequest request) {
        UUID id = UUID.fromString(userId);
        userService.deleteAccount(id, request.password());
        userPhotoService.deleteAllForUser(id);
    }

    @GetMapping("/me/export")
    public DataExportResponse exportCurrentUserData(@AuthenticationPrincipal String userId) {
        return dataExportService.export(UUID.fromString(userId));
    }

    @GetMapping("/me/discovery-preferences")
    public DiscoveryPreferenceResponse getDiscoveryPreferences(@AuthenticationPrincipal String userId) {
        return userMapper.toDiscoveryPreferenceResponse(
                userService.getDiscoveryPreference(UUID.fromString(userId)));
    }

    @PutMapping("/me/discovery-preferences")
    public DiscoveryPreferenceResponse updateDiscoveryPreferences(@AuthenticationPrincipal String userId,
                                                                    @Valid @RequestBody UpdateDiscoveryPreferenceRequest request) {
        return userMapper.toDiscoveryPreferenceResponse(
                userService.updateDiscoveryPreference(UUID.fromString(userId), request));
    }
}
