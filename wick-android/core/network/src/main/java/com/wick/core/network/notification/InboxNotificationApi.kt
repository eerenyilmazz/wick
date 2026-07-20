package com.wick.core.network.notification

import com.wick.core.network.dto.PageResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface InboxNotificationApi {

    @GET("api/v1/notifications")
    suspend fun getInbox(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageResponseDto<InboxNotificationDto>

    @POST("api/v1/notifications/read-all")
    suspend fun markAllRead()
}
