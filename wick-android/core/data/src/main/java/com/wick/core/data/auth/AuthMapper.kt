package com.wick.core.data.auth

import com.wick.core.data.photo.toDomain
import com.wick.core.model.Gender
import com.wick.core.model.User
import com.wick.core.network.dto.UserResponseDto

fun UserResponseDto.toDomain(baseUrl: String): User = User(
    id = id,
    email = email,
    displayName = displayName,
    dateOfBirth = dateOfBirth,
    gender = gender?.let { raw -> runCatching { Gender.valueOf(raw) }.getOrNull() },
    bio = bio,
    interests = interests,
    province = province,
    district = district,
    photos = photos.map { it.toDomain(baseUrl) },
    verified = verified,
    verificationStatus = verificationStatus,
    premium = premium,
    distanceKm = distanceKm,
    isNew = isNew,
)
