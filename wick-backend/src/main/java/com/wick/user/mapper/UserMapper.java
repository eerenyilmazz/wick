package com.wick.user.mapper;

import com.wick.photo.mapper.PhotoMapper;
import com.wick.photo.service.UserPhotoService;
import com.wick.security.TokenPair;
import com.wick.user.domain.DiscoveryPreference;
import com.wick.user.domain.User;
import com.wick.user.dto.AuthResponse;
import com.wick.user.dto.DiscoveryPreferenceResponse;
import com.wick.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private static final long NEW_USER_THRESHOLD_DAYS = 7;

    private final UserPhotoService userPhotoService;
    private final PhotoMapper photoMapper;

    public UserResponse toResponse(User user) {
        return toResponse(user, null);
    }

    public UserResponse toResponse(User user, Double distanceKm) {
        boolean isNew = user.getCreatedAt() != null
                && user.getCreatedAt().isAfter(Instant.now().minus(NEW_USER_THRESHOLD_DAYS, ChronoUnit.DAYS));
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getBio(),
                user.getInterests(),
                user.getProvince(),
                user.getDistrict(),
                userPhotoService.getForUser(user.getId()).stream().map(photoMapper::toResponse).toList(),
                user.isVerified(),
                user.getVerificationStatus(),
                user.isPremium(),
                distanceKm,
                isNew
        );
    }

    public AuthResponse toAuthResponse(User user, TokenPair tokens) {
        return new AuthResponse(tokens.accessToken(), tokens.refreshToken(), toResponse(user));
    }

    public DiscoveryPreferenceResponse toDiscoveryPreferenceResponse(DiscoveryPreference preference) {
        return new DiscoveryPreferenceResponse(
                preference.getMinAge(),
                preference.getMaxAge(),
                preference.getRadiusKm(),
                preference.getGenders()
        );
    }
}
