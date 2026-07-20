package com.wick.user.dto;

import com.wick.user.domain.Gender;

import java.util.Set;

public record DiscoveryPreferenceResponse(
        int minAge,
        int maxAge,
        Integer radiusKm,
        Set<Gender> genders
) {
}
