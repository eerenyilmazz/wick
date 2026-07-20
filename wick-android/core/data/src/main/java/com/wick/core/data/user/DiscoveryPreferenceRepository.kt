package com.wick.core.data.user

import com.wick.core.model.AppResult
import com.wick.core.model.DiscoveryPreference
import com.wick.core.model.Gender

interface DiscoveryPreferenceRepository {
    suspend fun getPreference(): AppResult<DiscoveryPreference>

    suspend fun updatePreference(
        minAge: Int,
        maxAge: Int,
        radiusKm: Int?,
        genders: Set<Gender>,
    ): AppResult<DiscoveryPreference>
}
