package com.wick.feature.auth.legal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wick.core.data.auth.AuthRepository
import com.wick.core.data.legal.LegalRepository
import com.wick.core.model.AppResult
import com.wick.core.model.LegalDocumentType
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
class LegalConsentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val legalRepository: LegalRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AuthRoute.LegalConsent>()

    private val _uiState = MutableStateFlow(LegalConsentUiState())
    val uiState: StateFlow<LegalConsentUiState> = _uiState.asStateFlow()

    private val _events = Channel<LegalConsentEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadDocuments()
    }

    fun onAction(action: LegalConsentAction) {
        when (action) {
            is LegalConsentAction.AcceptedChanged -> _uiState.value = _uiState.value.copy(accepted = action.value)
            LegalConsentAction.Submit -> submit()
        }
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            val privacyResult = legalRepository.getLatest(LegalDocumentType.PRIVACY_POLICY)
            val termsResult = legalRepository.getLatest(LegalDocumentType.TERMS_OF_SERVICE)

            if (privacyResult is AppResult.Success && termsResult is AppResult.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoadingDocuments = false,
                    privacyPolicy = privacyResult.data,
                    termsOfService = termsResult.data,
                )
            } else {
                val error = (privacyResult as? AppResult.Failure)?.error
                    ?: (termsResult as? AppResult.Failure)?.error
                _uiState.value = _uiState.value.copy(isLoadingDocuments = false, error = error)
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val privacyPolicy = state.privacyPolicy ?: return
        val termsOfService = state.termsOfService ?: return
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, error = null)

            val result = authRepository.register(
                email = args.email,
                password = args.password,
                displayName = args.displayName,
                acceptedPrivacyPolicyVersion = privacyPolicy.version,
                acceptedTermsVersion = termsOfService.version,
            )

            when (result) {
                is AppResult.Success -> {
                    authRepository.logout()
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    _events.send(LegalConsentEvent.NavigateToLogin)
                }

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.error,
                )
            }
        }
    }
}
