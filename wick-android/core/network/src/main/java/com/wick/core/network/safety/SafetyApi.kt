package com.wick.core.network.safety

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface SafetyApi {

    @POST("api/v1/users/{userId}/block")
    suspend fun block(@Path("userId") userId: String)

    @DELETE("api/v1/users/{userId}/block")
    suspend fun unblock(@Path("userId") userId: String)

    @POST("api/v1/users/{userId}/report")
    suspend fun report(@Path("userId") userId: String, @Body request: ReportRequestDto)
}
