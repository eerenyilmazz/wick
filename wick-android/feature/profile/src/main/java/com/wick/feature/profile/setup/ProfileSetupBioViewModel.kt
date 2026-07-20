package com.wick.feature.profile.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class ProfileSetupBioViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupBioUiState())
    val uiState: StateFlow<ProfileSetupBioUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileSetupBioEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ProfileSetupBioAction) {
        when (action) {
            is ProfileSetupBioAction.BioChanged -> _uiState.value = _uiState.value.copy(
                bio = action.value.take(BIO_MAX_LENGTH),
            )

            ProfileSetupBioAction.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, error = null)

            val result = profileRepository.updateProfile(bio = state.bio)

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    _events.send(ProfileSetupBioEvent.NavigateToDiscover)
                }

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.error,
                )
            }
        }
    }
}
