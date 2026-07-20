package com.wick.user.dto;

import com.wick.user.domain.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateDiscoveryPreferenceRequest(
        @NotNull @Min(18) @Max(100) Integer minAge,
        @NotNull @Min(18) @Max(100) Integer maxAge,
        @Min(1) @Max(500) Integer radiusKm,
        @NotNull Set<Gender> genders
) {
}
