package com.wick.feature.auth.register

import androidx.compose.runtime.Immutable
import com.wick.core.model.UiError

private val PASSWORD_STRENGTH_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")

@Immutable
data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isCheckingEmail: Boolean = false,
    val error: UiError? = null,
) {
    val isPasswordStrongEnough: Boolean
        get() = PASSWORD_STRENGTH_REGEX.matches(password)

    val doPasswordsMatch: Boolean
        get() = confirmPassword.isEmpty() || password == confirmPassword

    val canSubmit: Boolean
        get() = email.contains("@") &&
            isPasswordStrongEnough &&
            password == confirmPassword &&
            displayName.isNotBlank() &&
            !isCheckingEmail
}

sealed interface RegisterAction {
    data class EmailChanged(val value: String) : RegisterAction
    data class PasswordChanged(val value: String) : RegisterAction
    data class ConfirmPasswordChanged(val value: String) : RegisterAction
    data class DisplayNameChanged(val value: String) : RegisterAction
    data object Submit : RegisterAction
}

sealed interface RegisterEvent {
    data class NavigateToLegalConsent(
        val email: String,
        val password: String,
        val displayName: String,
    ) : RegisterEvent
}
