package com.wick.core.data.notification

import com.wick.core.model.AppResult
import com.wick.core.model.NotificationPreferences

interface NotificationPreferencesRepository {
    suspend fun getPreferences(): AppResult<NotificationPreferences>
    suspend fun updatePreferences(prefs: NotificationPreferences): AppResult<NotificationPreferences>
}
