package com.wick.core.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val dateOfBirth: String?,
    val gender: Gender?,
    val bio: String?,
    val interests: List<String>,
    val province: String?,
    val district: String?,
    val photos: List<Photo>,
    val verified: Boolean,
    val verificationStatus: String? = null,
    val premium: Boolean,
    val distanceKm: Double? = null,
    val isNew: Boolean = false,
)

enum class Gender {
    MALE, FEMALE, OTHER
}

val User.isProfileComplete: Boolean
    get() = dateOfBirth != null && gender != null && photos.isNotEmpty() && province != null &&
        district != null && !bio.isNullOrBlank() && interests.isNotEmpty()
