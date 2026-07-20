package com.wick.feature.profile.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.auth.AuthRepository
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

@Immutable
data class AccountSecurityUiState(
    val password: String = "",
    val isDeleting: Boolean = false,
    val hasError: Boolean = false,
)

sealed interface AccountSecurityEvent {
    data object NavigateToLogin : AccountSecurityEvent
}

@HiltViewModel
class AccountSecurityViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountSecurityUiState())
    val state: StateFlow<AccountSecurityUiState> = _state.asStateFlow()

    private val _events = Channel<AccountSecurityEvent>()
    val events = _events.receiveAsFlow()

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(password = value, hasError = false)
    }

    fun deleteAccount() {
        val password = _state.value.password
        if (password.isBlank() || _state.value.isDeleting) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true, hasError = false)
            when (profileRepository.deleteAccount(password)) {
                is AppResult.Success -> {
                    authRepository.logout()
                    _events.send(AccountSecurityEvent.NavigateToLogin)
                }
                is AppResult.Failure -> _state.value = _state.value.copy(
                    isDeleting = false,
                    hasError = true,
                )
            }
        }
    }
}
