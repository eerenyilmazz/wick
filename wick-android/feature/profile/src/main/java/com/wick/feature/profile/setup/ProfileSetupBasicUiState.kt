package com.wick.feature.profile.setup

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.wick.core.model.Gender
import com.wick.core.model.Photo
import com.wick.core.model.UiError
import java.time.LocalDate
import java.time.Period

const val MAX_INTERESTS = 10

@Immutable
data class ProfileSetupBasicUiState(
    val isLoadingConfig: Boolean = true,
    val minimumAge: Int = 18,
    val maximumAge: Int = 100,
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null,
    val showDatePicker: Boolean = false,
    val isLoadingPhotos: Boolean = true,
    val photos: List<Photo> = emptyList(),
    val maxPhotos: Int = 6,
    val isUploadingPhoto: Boolean = false,
    val interests: List<String> = emptyList(),
    val interestInput: String = "",
    val provinces: List<String> = emptyList(),
    val districts: List<String> = emptyList(),
    val isLoadingDistricts: Boolean = false,
    val province: String? = null,
    val district: String? = null,
    val showProvincePicker: Boolean = false,
    val showDistrictPicker: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: UiError? = null,
) {
    val age: Int?
        get() = dateOfBirth?.let { Period.between(it, LocalDate.now()).years }

    val isAgeValid: Boolean
        get() = age?.let { it in minimumAge..maximumAge } ?: false

    val canAddMorePhotos: Boolean
        get() = photos.size < maxPhotos

    val canAddMoreInterests: Boolean
        get() = interests.size < MAX_INTERESTS

    val canSubmit: Boolean
        get() = isAgeValid && gender != null && photos.isNotEmpty() &&
            province != null && district != null && interests.isNotEmpty() && !isSubmitting
}

sealed interface ProfileSetupBasicAction {
    data class DateOfBirthChanged(val value: LocalDate) : ProfileSetupBasicAction
    data class GenderChanged(val value: Gender) : ProfileSetupBasicAction
    data class DatePickerVisibilityChanged(val visible: Boolean) : ProfileSetupBasicAction
    data class PhotoPicked(val uri: Uri) : ProfileSetupBasicAction
    data class PhotosPicked(val uris: List<Uri>) : ProfileSetupBasicAction
    data class PhotoRemoved(val photoId: String) : ProfileSetupBasicAction
    data class PhotoMoved(val from: Int, val to: Int) : ProfileSetupBasicAction
    data class InterestInputChanged(val value: String) : ProfileSetupBasicAction
    data class InterestAdded(val value: String) : ProfileSetupBasicAction
    data class InterestRemoved(val value: String) : ProfileSetupBasicAction
    data class ProvincePickerVisibilityChanged(val visible: Boolean) : ProfileSetupBasicAction
    data class DistrictPickerVisibilityChanged(val visible: Boolean) : ProfileSetupBasicAction
    data class ProvinceSelected(val value: String) : ProfileSetupBasicAction
    data class DistrictSelected(val value: String) : ProfileSetupBasicAction
    data object Submit : ProfileSetupBasicAction
}

sealed interface ProfileSetupBasicEvent {
    data object NavigateToBio : ProfileSetupBasicEvent
}
