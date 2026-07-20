package com.wick.core.network.config

import retrofit2.http.GET

interface AppConfigApi {

    @GET("api/v1/config")
    suspend fun getConfig(): AppConfigResponseDto
}
