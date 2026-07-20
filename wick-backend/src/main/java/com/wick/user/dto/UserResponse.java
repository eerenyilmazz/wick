package com.wick.user.dto;

import com.wick.photo.dto.PhotoResponse;
import com.wick.user.domain.Gender;
import com.wick.user.domain.VerificationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        LocalDate dateOfBirth,
        Gender gender,
        String bio,
        List<String> interests,
        String province,
        String district,
        List<PhotoResponse> photos,
        boolean verified,
        VerificationStatus verificationStatus,
        boolean premium,
        Double distanceKm,
        boolean isNew
) {
}
