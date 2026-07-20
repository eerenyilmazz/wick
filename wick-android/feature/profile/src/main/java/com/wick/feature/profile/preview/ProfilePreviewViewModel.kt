package com.wick.feature.profile.preview

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.user.ProfileRepository
import com.wick.core.model.AppResult
import com.wick.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ProfilePreviewUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
)

@HiltViewModel
class ProfilePreviewViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilePreviewUiState())
    val uiState: StateFlow<ProfilePreviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = profileRepository.getCurrentUser()) {
                is AppResult.Success -> _uiState.value = ProfilePreviewUiState(isLoading = false, user = result.data)
                is AppResult.Failure -> _uiState.value = ProfilePreviewUiState(isLoading = false, user = null)
            }
        }
    }
}
