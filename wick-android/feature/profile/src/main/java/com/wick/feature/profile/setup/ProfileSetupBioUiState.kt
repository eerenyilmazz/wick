package com.wick.feature.profile.setup

import androidx.compose.runtime.Immutable
import com.wick.core.model.UiError

const val BIO_MAX_LENGTH = 500
const val BIO_MIN_LENGTH = 10

@Immutable
data class ProfileSetupBioUiState(
    val bio: String = "",
    val isSubmitting: Boolean = false,
    val error: UiError? = null,
) {
    val canSubmit: Boolean
        get() = bio.trim().length >= BIO_MIN_LENGTH && !isSubmitting
}

sealed interface ProfileSetupBioAction {
    data class BioChanged(val value: String) : ProfileSetupBioAction
    data object Submit : ProfileSetupBioAction
}

sealed interface ProfileSetupBioEvent {
    data object NavigateToDiscover : ProfileSetupBioEvent
}
