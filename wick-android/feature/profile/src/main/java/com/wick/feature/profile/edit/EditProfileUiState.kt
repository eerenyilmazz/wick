package com.wick.feature.profile.edit

import androidx.compose.runtime.Immutable
import com.wick.core.model.Gender
import com.wick.core.model.Photo
import com.wick.core.model.UiError
import com.wick.feature.profile.setup.BIO_MIN_LENGTH
import com.wick.feature.profile.setup.MAX_INTERESTS

@Immutable
data class EditProfileUiState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val bio: String = "",
    val gender: Gender? = null,
    val photos: List<Photo> = emptyList(),
    val pendingDeletePhotoIds: Set<String> = emptySet(),
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
    val visiblePhotos: List<Photo>
        get() = photos.filterNot { it.id in pendingDeletePhotoIds }

    val photosReordered: Boolean
        get() = visiblePhotos.map { it.id } != visiblePhotos.sortedBy { it.displayOrder }.map { it.id }

    val canAddMorePhotos: Boolean
        get() = visiblePhotos.size < maxPhotos

    val canAddMoreInterests: Boolean
        get() = interests.size < MAX_INTERESTS

    val canSubmit: Boolean
        get() = !isLoading && !isSubmitting && displayName.isNotBlank() &&
            bio.trim().length >= BIO_MIN_LENGTH && interests.isNotEmpty() && visiblePhotos.isNotEmpty()
}

sealed interface EditProfileAction {
    data class DisplayNameChanged(val value: String) : EditProfileAction
    data class BioChanged(val value: String) : EditProfileAction
    data class GenderChanged(val value: Gender) : EditProfileAction
    data class PhotoPicked(val uri: android.net.Uri) : EditProfileAction
    data class PhotosPicked(val uris: List<android.net.Uri>) : EditProfileAction
    data class PhotoRemoved(val photoId: String) : EditProfileAction
    data class PhotoSetAsCover(val photoId: String) : EditProfileAction
    data class PhotoMoved(val from: Int, val to: Int) : EditProfileAction
    data class InterestInputChanged(val value: String) : EditProfileAction
    data class InterestAdded(val value: String) : EditProfileAction
    data class InterestRemoved(val value: String) : EditProfileAction
    data class ProvincePickerVisibilityChanged(val visible: Boolean) : EditProfileAction
    data class DistrictPickerVisibilityChanged(val visible: Boolean) : EditProfileAction
    data class ProvinceSelected(val value: String) : EditProfileAction
    data class DistrictSelected(val value: String) : EditProfileAction
    data object Submit : EditProfileAction
}

sealed interface EditProfileEvent {
    data object NavigateBack : EditProfileEvent
}
