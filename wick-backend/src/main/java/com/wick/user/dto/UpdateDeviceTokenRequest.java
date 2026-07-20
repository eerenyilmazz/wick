package com.wick.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceTokenRequest(@NotBlank String fcmToken) {
}
