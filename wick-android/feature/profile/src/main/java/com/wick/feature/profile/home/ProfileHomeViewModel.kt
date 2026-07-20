package com.wick.feature.profile.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.auth.AuthRepository
import com.wick.core.data.notification.InboxNotificationRepository
import com.wick.core.data.token.TokenRepository
import com.wick.core.data.user.ProfileRepository
import com.wick.core.model.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileHomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository,
    private val inboxNotificationRepository: InboxNotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileHomeUiState())
    val uiState: StateFlow<ProfileHomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileHomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadUser()
        loadBalance()
        checkUnreadNotifications()
    }

    fun refresh() {
        loadUser()
        loadBalance()
    }

    private fun checkUnreadNotifications() {
        viewModelScope.launch {
            inboxNotificationRepository.observeUnreadCount().collect { count ->
                _uiState.value = _uiState.value.copy(unreadNotificationCount = count)
            }
        }
    }

    fun onAction(action: ProfileHomeAction) {
        when (action) {
            ProfileHomeAction.LogoutRequested -> _uiState.value = _uiState.value.copy(
                showLogoutConfirmation = true,
            )

            ProfileHomeAction.LogoutCancelled -> _uiState.value = _uiState.value.copy(
                showLogoutConfirmation = false,
            )

            ProfileHomeAction.LogoutConfirmed -> logout()

            ProfileHomeAction.ExportDataRequested -> exportData()
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            when (val result = profileRepository.getCurrentUser()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = result.data,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.error,
                )
            }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            val result = tokenRepository.getBalance()
            if (result is AppResult.Success) {
                _uiState.value = _uiState.value.copy(tokenBalance = result.data)
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(showLogoutConfirmation = false)
            _events.send(ProfileHomeEvent.NavigateToLogin)
        }
    }

    private fun exportData() {
        if (_uiState.value.isExporting) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportSuccess = false)
            when (profileRepository.exportData()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = true,
                )
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = false,
                    error = com.wick.core.model.UiError.Unknown,
                )
            }
        }
    }
}
