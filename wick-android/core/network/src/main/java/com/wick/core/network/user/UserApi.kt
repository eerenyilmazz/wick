package com.wick.core.network.user

import com.wick.core.network.dto.UserResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT

interface UserApi {

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): UserResponseDto

    @PUT("api/v1/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): UserResponseDto

    @HTTP(method = "DELETE", path = "api/v1/users/me", hasBody = true)
    suspend fun deleteAccount(@Body request: DeleteAccountRequestDto)

    @GET("api/v1/users/me/discovery-preferences")
    suspend fun getDiscoveryPreferences(): DiscoveryPreferenceResponseDto

    @PUT("api/v1/users/me/discovery-preferences")
    suspend fun updateDiscoveryPreferences(
        @Body request: UpdateDiscoveryPreferenceRequestDto,
    ): DiscoveryPreferenceResponseDto

    @PUT("api/v1/users/me/device-token")
    suspend fun updateDeviceToken(@Body request: UpdateDeviceTokenRequestDto)

    @Multipart
    @POST("api/v1/profile/verify")
    suspend fun submitVerification(@Part photo: MultipartBody.Part)

    @GET("api/v1/users/me/export")
    suspend fun exportData(): DataExportResponseDto
}
