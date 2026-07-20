package com.wick.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.auth.AuthRepository
import com.wick.core.model.AppResult
import com.wick.core.model.isProfileComplete
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> _uiState.value = _uiState.value.copy(
                email = action.value,
                error = null,
            )

            is LoginAction.PasswordChanged -> _uiState.value = _uiState.value.copy(
                password = action.value,
                error = null,
            )

            is LoginAction.KeepSignedInChanged -> _uiState.value = _uiState.value.copy(
                keepSignedIn = action.value,
            )

            LoginAction.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            when (val result = authRepository.login(state.email, state.password, state.keepSignedIn)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    val user = result.data
                    _events.send(
                        if (user.isProfileComplete) LoginEvent.NavigateToDiscover else LoginEvent.NavigateToProfileSetup,
                    )
                }

                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error,
                    )
                }
            }
        }
    }
}
