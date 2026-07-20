package com.wick.user.dto;

import com.wick.user.domain.Gender;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateProfileRequest(
        @Size(min = 1, max = 50) String displayName,
        @Past LocalDate dateOfBirth,
        Gender gender,
        @Size(max = 500) String bio,
        @Size(max = 10) List<@Size(max = 50) String> interests,
        @Size(max = 50) String province,
        @Size(max = 50) String district,
        @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @DecimalMin("-180") @DecimalMax("180") Double longitude
) {
}
