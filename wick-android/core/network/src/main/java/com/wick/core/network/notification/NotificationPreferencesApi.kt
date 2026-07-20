package com.wick.core.network.notification

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface NotificationPreferencesApi {

    @GET("api/v1/notification-preferences")
    suspend fun getPreferences(): NotificationPreferencesDto

    @PUT("api/v1/notification-preferences")
    suspend fun updatePreferences(@Body request: NotificationPreferencesDto): NotificationPreferencesDto
}
