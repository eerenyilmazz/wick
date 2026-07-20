package com.wick.core.network.dto

import com.wick.core.network.photo.PhotoResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class EmailAvailabilityResponseDto(
    val available: Boolean,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String,
    val acceptedPrivacyPolicyVersion: Int,
    val acceptedTermsVersion: Int,
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponseDto,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
data class TokenPairResponseDto(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class UserResponseDto(
    val id: String,
    val email: String,
    val displayName: String,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val bio: String? = null,
    val interests: List<String> = emptyList(),
    val province: String? = null,
    val district: String? = null,
    val photos: List<PhotoResponseDto> = emptyList(),
    val verified: Boolean,
    val verificationStatus: String? = null,
    val premium: Boolean,
    val distanceKm: Double? = null,
    val isNew: Boolean = false,
)
