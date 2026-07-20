package com.wick.feature.profile.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.legal.LegalRepository
import com.wick.core.model.AppResult
import com.wick.core.model.LegalDocumentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LegalDocumentsViewModel @Inject constructor(
    private val legalRepository: LegalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LegalDocumentsUiState())
    val uiState: StateFlow<LegalDocumentsUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            val privacyResult = legalRepository.getLatest(LegalDocumentType.PRIVACY_POLICY)
            val termsResult = legalRepository.getLatest(LegalDocumentType.TERMS_OF_SERVICE)

            if (privacyResult is AppResult.Success && termsResult is AppResult.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    privacyPolicy = privacyResult.data,
                    termsOfService = termsResult.data,
                )
            } else {
                val error = (privacyResult as? AppResult.Failure)?.error
                    ?: (termsResult as? AppResult.Failure)?.error
                _uiState.value = _uiState.value.copy(isLoading = false, error = error)
            }
        }
    }
}
