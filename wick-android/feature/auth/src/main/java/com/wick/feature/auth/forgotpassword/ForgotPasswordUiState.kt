package com.wick.feature.auth.forgotpassword

import androidx.compose.runtime.Immutable
import com.wick.core.model.UiError

@Immutable
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: UiError? = null,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && email.contains("@") && !isLoading
}

sealed interface ForgotPasswordAction {
    data class EmailChanged(val value: String) : ForgotPasswordAction
    data object Submit : ForgotPasswordAction
}

sealed interface ForgotPasswordEvent {
    data class NavigateToEmailSent(val email: String) : ForgotPasswordEvent
}
