package com.wick.core.data.auth

import com.wick.core.model.AppResult
import com.wick.core.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String, keepSignedIn: Boolean = true): AppResult<User>

    suspend fun isEmailAvailable(email: String): AppResult<Boolean>

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        acceptedPrivacyPolicyVersion: Int,
        acceptedTermsVersion: Int,
    ): AppResult<User>

    suspend fun logout()

    suspend fun forgotPassword(email: String): AppResult<Unit>

    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit>

    fun hasSession(): Boolean

    val sessionExpiredFlow: Flow<Unit>
}
