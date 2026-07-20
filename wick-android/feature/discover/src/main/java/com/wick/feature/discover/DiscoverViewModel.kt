package com.wick.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.config.ConfigRepository
import com.wick.core.data.invitation.InvitationRepository
import com.wick.core.data.match.DiscoverRepository
import com.wick.core.data.safety.SafetyRepository
import com.wick.core.data.user.DiscoveryPreferenceRepository
import com.wick.core.data.user.ProfileRepository
import com.wick.core.model.AppResult
import com.wick.core.model.Gender
import com.wick.core.model.ReportReason
import com.wick.core.model.SwipeDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val discoverRepository: DiscoverRepository,
    private val safetyRepository: SafetyRepository,
    private val profileRepository: ProfileRepository,
    private val discoveryPreferenceRepository: DiscoveryPreferenceRepository,
    private val invitationRepository: InvitationRepository,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
        loadOwnPhoto()
        viewModelScope.launch {
            val result = configRepository.getConfig()
            if (result is AppResult.Success) {
                _uiState.value = _uiState.value.copy(superLikeTokenCost = result.data.superLikeTokenCost)
            }
        }
    }

    private fun sendInviteAndDismiss() {
        val matchId = _uiState.value.matchedId ?: return
        _uiState.value = _uiState.value.copy(matchedDisplayName = null, matchedPhotoUrl = null, matchedId = null)
        viewModelScope.launch { invitationRepository.sendInvitation(matchId) }
    }

    fun onAction(action: DiscoverAction) {
        when (action) {
            DiscoverAction.Like -> swipe(SwipeDirection.LIKE)
            DiscoverAction.SuperLike -> swipe(SwipeDirection.SUPER_LIKE)
            DiscoverAction.Pass -> swipe(SwipeDirection.PASS)
            DiscoverAction.Retry -> loadUsers()
            DiscoverAction.MatchDismissed -> _uiState.value = _uiState.value.copy(
                matchedDisplayName = null,
                matchedPhotoUrl = null,
                matchedId = null,
            )
            DiscoverAction.SendInvite -> sendInviteAndDismiss()
            is DiscoverAction.Block -> block(action.userId)
            is DiscoverAction.Report -> report(action.userId, action.reason)
            DiscoverAction.OpenFilters -> openFilters()
            DiscoverAction.DismissFilters -> _uiState.value = _uiState.value.copy(filterSheetVisible = false)
            is DiscoverAction.FilterMinAgeChanged -> _uiState.value =
                _uiState.value.copy(filterMinAge = action.value)
            is DiscoverAction.FilterMaxAgeChanged -> _uiState.value =
                _uiState.value.copy(filterMaxAge = action.value)
            is DiscoverAction.FilterRadiusChanged -> _uiState.value =
                _uiState.value.copy(filterRadiusKm = action.value)
            is DiscoverAction.FilterGenderToggled -> toggleFilterGender(action.gender)
            is DiscoverAction.FilterGenderSet -> _uiState.value = _uiState.value.copy(filterGenders = action.genders)
            DiscoverAction.ResetFilters -> _uiState.value = _uiState.value.copy(
                filterMinAge = 18,
                filterMaxAge = 100,
                filterRadiusKm = if (_uiState.value.hasSharedLocation) 300 else null,
                filterGenders = emptySet(),
            )
            DiscoverAction.ApplyFilters -> applyFilters()
            DiscoverAction.RequestLocation -> _uiState.value =
                _uiState.value.copy(isRequestingLocation = true, locationDenied = false)
            is DiscoverAction.LocationReceived -> updateLocation(action.latitude, action.longitude)
            DiscoverAction.LocationDenied -> _uiState.value =
                _uiState.value.copy(isRequestingLocation = false, locationDenied = true)
            DiscoverAction.ClearReportSuccess -> clearReportSuccess()
        }
    }

    private fun openFilters() {
        _uiState.value = _uiState.value.copy(filterSheetVisible = true, isFilterLoading = true)
        viewModelScope.launch {
            when (val result = discoveryPreferenceRepository.getPreference()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isFilterLoading = false,
                    filterMinAge = result.data.minAge,
                    filterMaxAge = result.data.maxAge,
                    filterRadiusKm = result.data.radiusKm,
                    filterGenders = result.data.genders,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isFilterLoading = false,
                    error = result.error,
                )
            }
        }
    }

    private fun updateLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val result = profileRepository.updateProfile(latitude = latitude, longitude = longitude)
            _uiState.value = _uiState.value.copy(
                isRequestingLocation = false,
                hasSharedLocation = result is AppResult.Success,
                error = if (result is AppResult.Failure) result.error else _uiState.value.error,
            )
        }
    }

    private fun toggleFilterGender(gender: Gender) {
        val current = _uiState.value.filterGenders
        _uiState.value = _uiState.value.copy(
            filterGenders = if (gender in current) current - gender else current + gender,
        )
    }

    private fun applyFilters() {
        val state = _uiState.value
        if (state.filterMinAge > state.filterMaxAge) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFilterSaving = true)
            val result = discoveryPreferenceRepository.updatePreference(
                minAge = state.filterMinAge,
                maxAge = state.filterMaxAge,
                radiusKm = state.filterRadiusKm,
                genders = state.filterGenders,
            )
            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isFilterSaving = false, filterSheetVisible = false)
                    loadUsers()
                }

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isFilterSaving = false,
                    error = result.error,
                )
            }
        }
    }

    private fun loadOwnPhoto() {
        viewModelScope.launch {
            val result = profileRepository.getCurrentUser()
            if (result is AppResult.Success) {
                _uiState.value = _uiState.value.copy(
                    ownPhotoUrl = result.data.photos.firstOrNull()?.url,
                )
            }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = discoverRepository.getDiscoverableUsers()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    users = result.data,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.error,
                )
            }
        }
    }

    private fun swipe(direction: SwipeDirection) {
        val state = _uiState.value
        val target = state.currentUser ?: return
        if (state.isSwiping) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSwiping = true, error = null)

            when (val result = discoverRepository.swipe(target.id, direction)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isSwiping = false,
                    users = _uiState.value.users.filterNot { it.id == target.id },
                    matchedDisplayName = if (result.data.matched) target.displayName else null,
                    matchedPhotoUrl = if (result.data.matched) target.photos.firstOrNull()?.url else null,
                    matchedId = if (result.data.matched) result.data.matchId else null,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isSwiping = false,
                    error = result.error,
                )
            }
        }
    }

    private fun block(userId: String) {
        viewModelScope.launch {
            when (val result = safetyRepository.block(userId)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    users = _uiState.value.users.filterNot { it.id == userId },
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(error = result.error)
            }
        }
    }

    fun clearReportSuccess() {
        _uiState.value = _uiState.value.copy(reportedSuccessfully = false)
    }

    private fun report(userId: String, reason: ReportReason) {
        viewModelScope.launch {
            when (val result = safetyRepository.report(userId, reason, null)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    users = _uiState.value.users.filterNot { it.id == userId },
                    reportedSuccessfully = true,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(error = result.error)
            }
        }
    }
}
