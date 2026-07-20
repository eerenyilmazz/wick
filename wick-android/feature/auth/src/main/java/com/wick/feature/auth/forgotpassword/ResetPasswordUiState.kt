package com.wick.feature.auth.forgotpassword

import androidx.compose.runtime.Immutable
import com.wick.core.model.UiError

private const val MIN_PASSWORD_LENGTH = 8

@Immutable
data class ResetPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: UiError? = null,
) {
    val isPasswordLongEnough: Boolean
        get() = newPassword.length >= MIN_PASSWORD_LENGTH

    val doPasswordsMatch: Boolean
        get() = confirmPassword.isEmpty() || newPassword == confirmPassword

    val canSubmit: Boolean
        get() = isPasswordLongEnough && newPassword == confirmPassword && !isLoading
}

sealed interface ResetPasswordAction {
    data class NewPasswordChanged(val value: String) : ResetPasswordAction
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordAction
    data object Submit : ResetPasswordAction
}

sealed interface ResetPasswordEvent {
    data object NavigateToLogin : ResetPasswordEvent
}
