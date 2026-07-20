package com.wick.core.network.legal

import retrofit2.http.GET
import retrofit2.http.Path

interface LegalApi {

    @GET("api/v1/legal/{type}")
    suspend fun getLatest(@Path("type") type: String): LegalDocumentResponseDto
}
