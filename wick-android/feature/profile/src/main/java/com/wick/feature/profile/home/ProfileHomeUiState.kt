package com.wick.feature.profile.home

import androidx.compose.runtime.Immutable
import com.wick.core.model.UiError
import com.wick.core.model.User

@Immutable
data class ProfileHomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val tokenBalance: Int? = null,
    val showLogoutConfirmation: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val error: UiError? = null,
    val unreadNotificationCount: Int = 0,
)

sealed interface ProfileHomeAction {
    data object LogoutRequested : ProfileHomeAction
    data object LogoutCancelled : ProfileHomeAction
    data object LogoutConfirmed : ProfileHomeAction
    data object ExportDataRequested : ProfileHomeAction
}

sealed interface ProfileHomeEvent {
    data object NavigateToLogin : ProfileHomeEvent
}
