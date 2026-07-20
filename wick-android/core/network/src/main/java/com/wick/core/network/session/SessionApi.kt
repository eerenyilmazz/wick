package com.wick.core.network.session

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SessionApi {

    @GET("api/v1/sessions/my")
    suspend fun getMySessions(): List<ConversationSummaryDto>

    @GET("api/v1/sessions/active")
    suspend fun getActiveForMatch(@Query("matchId") matchId: String): SessionResponseDto

    @GET("api/v1/sessions/for-match")
    suspend fun getLatestForMatch(@Query("matchId") matchId: String): SessionResponseDto

    @GET("api/v1/sessions/{id}/messages")
    suspend fun getMessages(
        @Path("id") sessionId: String,
        @Query("since") since: String? = null,
    ): List<MessageResponseDto>

    @POST("api/v1/sessions/{id}/extend")
    suspend fun extend(@Path("id") sessionId: String): SessionResponseDto

    @POST("api/v1/sessions/{id}/end")
    suspend fun end(@Path("id") sessionId: String)
}
