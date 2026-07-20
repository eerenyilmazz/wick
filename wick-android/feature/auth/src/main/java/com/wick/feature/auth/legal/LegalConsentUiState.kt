package com.wick.feature.auth.legal

import androidx.compose.runtime.Immutable
import com.wick.core.model.LegalDocument
import com.wick.core.model.UiError

@Immutable
data class LegalConsentUiState(
    val isLoadingDocuments: Boolean = true,
    val privacyPolicy: LegalDocument? = null,
    val termsOfService: LegalDocument? = null,
    val accepted: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: UiError? = null,
) {
    val canSubmit: Boolean
        get() = accepted && privacyPolicy != null && termsOfService != null && !isSubmitting
}

sealed interface LegalConsentAction {
    data class AcceptedChanged(val value: Boolean) : LegalConsentAction
    data object Submit : LegalConsentAction
}

sealed interface LegalConsentEvent {
    data object NavigateToLogin : LegalConsentEvent
}
