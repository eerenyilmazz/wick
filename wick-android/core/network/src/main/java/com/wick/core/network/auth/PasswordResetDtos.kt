package com.wick.core.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequestDto(val email: String)

@Serializable
data class ResetPasswordRequestDto(val token: String, val newPassword: String)
