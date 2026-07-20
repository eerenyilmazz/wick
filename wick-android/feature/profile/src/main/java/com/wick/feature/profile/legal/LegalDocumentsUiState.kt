package com.wick.feature.profile.legal

import androidx.compose.runtime.Immutable
import com.wick.core.model.LegalDocument
import com.wick.core.model.UiError

@Immutable
data class LegalDocumentsUiState(
    val isLoading: Boolean = true,
    val privacyPolicy: LegalDocument? = null,
    val termsOfService: LegalDocument? = null,
    val error: UiError? = null,
)
