package com.wick.feature.profile.verification

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.user.ProfileRepository
import com.wick.core.model.AppResult
import com.wick.core.model.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class VerificationUiState(
    val isCheckingStatus: Boolean = false,
    val isUploading: Boolean = false,
    val isSubmitted: Boolean = false,
    val isRejected: Boolean = false,
    val error: UiError? = null,
    val capturedUriString: String? = null,
)

@HiltViewModel
class AccountVerificationViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VerificationUiState(isCheckingStatus = true))
    val state: StateFlow<VerificationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val result = profileRepository.getCurrentUser()
                val status = if (result is AppResult.Success) result.data.verificationStatus else null
                _state.value = when (status) {
                    "PENDING", "VERIFIED" -> VerificationUiState(isCheckingStatus = false, isSubmitted = true)
                    "REJECTED" -> VerificationUiState(isCheckingStatus = false, isRejected = true)
                    else -> VerificationUiState(isCheckingStatus = false)
                }
            } catch (e: Exception) {
                _state.value = VerificationUiState(isCheckingStatus = false)
            }
        }
    }

    fun dismissRejected() {
        _state.value = _state.value.copy(isRejected = false)
    }

    fun onCaptured(uri: Uri) {
        _state.value = _state.value.copy(capturedUriString = uri.toString(), error = null)
    }

    fun retake() {
        _state.value = _state.value.copy(capturedUriString = null, error = null)
    }

    fun submit() {
        val uriString = _state.value.capturedUriString ?: return
        if (_state.value.isUploading || _state.value.isSubmitted) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, error = null)
            try {
                val result = profileRepository.submitVerification(Uri.parse(uriString))
                when (result) {
                    is AppResult.Success -> _state.value = VerificationUiState(isSubmitted = true)
                    is AppResult.Failure -> _state.value = _state.value.copy(isUploading = false, error = result.error)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isUploading = false, error = UiError.Unknown)
            }
        }
    }
}
