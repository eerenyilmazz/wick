package com.wick.user.service;

import com.wick.user.domain.DiscoveryPreference;
import com.wick.user.domain.User;
import com.wick.user.dto.RegisterRequest;
import com.wick.user.dto.UpdateDiscoveryPreferenceRequest;
import com.wick.user.dto.UpdateProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    User register(RegisterRequest request);

    boolean isEmailAvailable(String email);

    User authenticate(String email, String rawPassword);

    User getById(UUID id);

    Page<User> getDiscoverableUsers(UUID userId, Pageable pageable);

    DiscoveryPreference getDiscoveryPreference(UUID userId);

    DiscoveryPreference updateDiscoveryPreference(UUID userId, UpdateDiscoveryPreferenceRequest request);

    User updateProfile(UUID id, UpdateProfileRequest request);

    User verify(UUID id);

    User grantPremium(UUID id);

    User revokePremium(UUID id);

    User updateDeviceToken(UUID id, String fcmToken);

    void deleteAccount(UUID id, String rawPassword);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}
