package com.wick.core.network.photo

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface PhotoApi {

    @GET("api/v1/users/me/photos")
    suspend fun getPhotos(): List<PhotoResponseDto>

    @Multipart
    @retrofit2.http.POST("api/v1/users/me/photos")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): PhotoResponseDto

    @DELETE("api/v1/users/me/photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: String)

    @PUT("api/v1/users/me/photos/order")
    suspend fun reorderPhotos(@Body request: ReorderPhotosRequestDto): List<PhotoResponseDto>
}
