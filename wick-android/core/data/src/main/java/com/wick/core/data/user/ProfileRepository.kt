package com.wick.core.data.user

import android.net.Uri
import com.wick.core.model.AppResult
import com.wick.core.model.Gender
import com.wick.core.model.User
import java.time.LocalDate

interface ProfileRepository {
    suspend fun getCurrentUser(): AppResult<User>

    suspend fun updateProfile(
        dateOfBirth: LocalDate? = null,
        gender: Gender? = null,
        bio: String? = null,
        interests: List<String>? = null,
        displayName: String? = null,
        province: String? = null,
        district: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
    ): AppResult<User>

    suspend fun deleteAccount(password: String): AppResult<Unit>

    suspend fun submitVerification(photoUri: Uri): AppResult<Unit>

    suspend fun exportData(): AppResult<Unit>
}
