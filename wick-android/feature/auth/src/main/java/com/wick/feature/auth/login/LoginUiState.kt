package com.wick.feature.auth.login

import androidx.compose.runtime.Immutable
import com.wick.core.model.UiError

@Immutable
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val keepSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiError? = null,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface LoginAction {
    data class EmailChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data class KeepSignedInChanged(val value: Boolean) : LoginAction
    data object Submit : LoginAction
}

sealed interface LoginEvent {
    data object NavigateToDiscover : LoginEvent
    data object NavigateToProfileSetup : LoginEvent
}
