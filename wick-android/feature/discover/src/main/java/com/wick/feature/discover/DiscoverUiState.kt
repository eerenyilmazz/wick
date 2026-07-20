package com.wick.feature.discover

import androidx.compose.runtime.Immutable
import com.wick.core.model.Gender
import com.wick.core.model.ReportReason
import com.wick.core.model.UiError
import com.wick.core.model.User

@Immutable
data class DiscoverUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val isSwiping: Boolean = false,
    val matchedDisplayName: String? = null,
    val matchedPhotoUrl: String? = null,
    val matchedId: String? = null,
    val ownPhotoUrl: String? = null,
    val error: UiError? = null,
    val reportedSuccessfully: Boolean = false,
    val filterSheetVisible: Boolean = false,
    val isFilterLoading: Boolean = false,
    val isFilterSaving: Boolean = false,
    val filterMinAge: Int = 18,
    val filterMaxAge: Int = 100,
    val filterRadiusKm: Int? = null,
    val filterGenders: Set<Gender> = emptySet(),
    val hasSharedLocation: Boolean = false,
    val isRequestingLocation: Boolean = false,
    val locationDenied: Boolean = false,
    val superLikeTokenCost: Int = 1,
) {
    val currentUser: User?
        get() = users.firstOrNull()
}

sealed interface DiscoverAction {
    data object Like : DiscoverAction
    data object SuperLike : DiscoverAction
    data object Pass : DiscoverAction
    data object Retry : DiscoverAction
    data object MatchDismissed : DiscoverAction
    data object SendInvite : DiscoverAction
    data class Block(val userId: String) : DiscoverAction
    data class Report(val userId: String, val reason: ReportReason) : DiscoverAction
    data object OpenFilters : DiscoverAction
    data object DismissFilters : DiscoverAction
    data class FilterMinAgeChanged(val value: Int) : DiscoverAction
    data class FilterMaxAgeChanged(val value: Int) : DiscoverAction
    data class FilterRadiusChanged(val value: Int?) : DiscoverAction
    data class FilterGenderToggled(val gender: Gender) : DiscoverAction
    data class FilterGenderSet(val genders: Set<Gender>) : DiscoverAction
    data object ResetFilters : DiscoverAction
    data object ApplyFilters : DiscoverAction
    data object RequestLocation : DiscoverAction
    data class LocationReceived(val latitude: Double, val longitude: Double) : DiscoverAction
    data object LocationDenied : DiscoverAction
    data object ClearReportSuccess : DiscoverAction
}
