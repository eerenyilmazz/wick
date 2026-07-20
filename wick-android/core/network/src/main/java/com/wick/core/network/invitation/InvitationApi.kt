package com.wick.core.network.invitation

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface InvitationApi {

    @POST("api/v1/invitations")
    suspend fun send(@Body request: SendInvitationRequestDto): InvitationResponseDto

    @GET("api/v1/invitations/incoming")
    suspend fun getIncoming(): List<InvitationResponseDto>

    @POST("api/v1/invitations/{id}/accept")
    suspend fun accept(@Path("id") id: String): InvitationResponseDto

    @POST("api/v1/invitations/{id}/decline")
    suspend fun decline(@Path("id") id: String): InvitationResponseDto

    @DELETE("api/v1/invitations/{id}")
    suspend fun cancel(@Path("id") id: String): InvitationResponseDto
}
