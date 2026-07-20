package com.wick.core.data.notification

import com.wick.core.model.AppResult
import com.wick.core.model.NotificationPreferences
import com.wick.core.model.UiError
import com.wick.core.network.notification.NotificationPreferencesApi
import com.wick.core.network.notification.NotificationPreferencesDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class NotificationPreferencesRepositoryImpl @Inject constructor(
    private val api: NotificationPreferencesApi,
) : NotificationPreferencesRepository {

    override suspend fun getPreferences(): AppResult<NotificationPreferences> =
        runCatching { api.getPreferences().toDomain() }
            .fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { it.toResult() },
            )

    override suspend fun updatePreferences(prefs: NotificationPreferences): AppResult<NotificationPreferences> =
        runCatching { api.updatePreferences(prefs.toDto()).toDomain() }
            .fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { it.toResult() },
            )

    private fun NotificationPreferencesDto.toDomain() = NotificationPreferences(
        newMatches = newMatches,
        invites = invites,
        messages = messages,
        reminders = reminders,
        promos = promos,
    )

    private fun NotificationPreferences.toDto() = NotificationPreferencesDto(
        newMatches = newMatches,
        invites = invites,
        messages = messages,
        reminders = reminders,
        promos = promos,
    )

    private fun <T> Throwable.toResult(): AppResult<T> = when (this) {
        is HttpException -> AppResult.Failure(if (code() == 401) UiError.Unauthorized else UiError.Unknown)
        is IOException -> AppResult.Failure(UiError.NoConnection)
        else -> throw this
    }
}
