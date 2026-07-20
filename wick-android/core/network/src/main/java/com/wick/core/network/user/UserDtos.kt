package com.wick.core.network.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val bio: String? = null,
    val interests: List<String>? = null,
    val province: String? = null,
    val district: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class DeleteAccountRequestDto(val password: String)

@Serializable
data class UpdateDeviceTokenRequestDto(val fcmToken: String)

@Serializable
data class DiscoveryPreferenceResponseDto(
    val minAge: Int,
    val maxAge: Int,
    val radiusKm: Int? = null,
    val genders: List<String> = emptyList(),
)

@Serializable
data class UpdateDiscoveryPreferenceRequestDto(
    val minAge: Int,
    val maxAge: Int,
    val radiusKm: Int? = null,
    val genders: List<String> = emptyList(),
)
