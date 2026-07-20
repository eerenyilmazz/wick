package com.wick.core.network.auth

import com.wick.core.network.dto.AuthResponseDto
import com.wick.core.network.dto.EmailAvailabilityResponseDto
import com.wick.core.network.dto.LoginRequestDto
import com.wick.core.network.dto.RefreshTokenRequestDto
import com.wick.core.network.dto.RegisterRequestDto
import com.wick.core.network.dto.TokenPairResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @GET("api/v1/auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): EmailAvailabilityResponseDto

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): TokenPairResponseDto

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequestDto)

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto)

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto)
}
