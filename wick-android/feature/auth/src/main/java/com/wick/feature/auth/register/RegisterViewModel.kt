package com.wick.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.auth.AuthRepository
import com.wick.core.model.AppResult
import com.wick.core.model.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.EmailChanged -> _uiState.value = _uiState.value.copy(email = action.value)
            is RegisterAction.PasswordChanged -> _uiState.value = _uiState.value.copy(password = action.value)
            is RegisterAction.ConfirmPasswordChanged -> _uiState.value = _uiState.value.copy(
                confirmPassword = action.value,
            )
            is RegisterAction.DisplayNameChanged -> _uiState.value = _uiState.value.copy(displayName = action.value)
            RegisterAction.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.value = state.copy(isCheckingEmail = true, error = null)

            when (val result = authRepository.isEmailAvailable(state.email)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isCheckingEmail = false)
                    if (result.data) {
                        _events.send(
                            RegisterEvent.NavigateToLegalConsent(
                                email = state.email,
                                password = state.password,
                                displayName = state.displayName,
                            ),
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(error = UiError.Conflict)
                    }
                }

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isCheckingEmail = false,
                    error = result.error,
                )
            }
        }
    }
}
