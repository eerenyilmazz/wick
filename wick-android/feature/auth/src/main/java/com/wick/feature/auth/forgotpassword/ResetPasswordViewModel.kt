package com.wick.feature.auth.forgotpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wick.core.data.auth.AuthRepository
import com.wick.core.model.AppResult
import com.wick.feature.auth.navigation.AuthRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val token = savedStateHandle.toRoute<AuthRoute.ResetPassword>().token

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<ResetPasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ResetPasswordAction) {
        when (action) {
            is ResetPasswordAction.NewPasswordChanged -> _uiState.value = _uiState.value.copy(
                newPassword = action.value, error = null,
            )
            is ResetPasswordAction.ConfirmPasswordChanged -> _uiState.value = _uiState.value.copy(
                confirmPassword = action.value,
            )
            ResetPasswordAction.Submit -> submit()
        }
    }

    private fun submit() {
        if (!_uiState.value.canSubmit) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.resetPassword(token, _uiState.value.newPassword)) {
                is AppResult.Success -> _events.send(ResetPasswordEvent.NavigateToLogin)
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.error,
                )
            }
        }
    }
}
