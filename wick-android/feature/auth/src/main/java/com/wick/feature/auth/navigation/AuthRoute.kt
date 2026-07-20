package com.wick.feature.auth.navigation

import kotlinx.serialization.Serializable

sealed interface AuthRoute {
    @Serializable
    data class Login(val registered: Boolean = false) : AuthRoute

    @Serializable
    data object Register : AuthRoute

    @Serializable
    data class LegalConsent(
        val email: String,
        val password: String,
        val displayName: String,
    ) : AuthRoute

    @Serializable
    data object ForgotPassword : AuthRoute

    @Serializable
    data class EmailSent(val email: String) : AuthRoute

    @Serializable
    data class ResetPassword(val token: String) : AuthRoute

    @Serializable
    data object LocationPermission : AuthRoute

    @Serializable
    data object NotificationPermission : AuthRoute
}
