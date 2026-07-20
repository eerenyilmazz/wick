package com.wick.core.model

data class DiscoveryPreference(
    val minAge: Int,
    val maxAge: Int,
    val radiusKm: Int?,
    val genders: Set<Gender>,
)
