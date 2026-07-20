package com.wick.feature.profile.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.config.ConfigRepository
import com.wick.core.data.location.LocationRepository
import com.wick.core.data.photo.PhotoRepository
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
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val photoRepository: PhotoRepository,
    private val locationRepository: LocationRepository,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadConfig()
        loadProvinces()
        loadUser()
    }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.DisplayNameChanged -> _uiState.value = _uiState.value.copy(
                displayName = action.value,
            )

            is EditProfileAction.BioChanged -> _uiState.value = _uiState.value.copy(bio = action.value)
            is EditProfileAction.GenderChanged -> _uiState.value = _uiState.value.copy(gender = action.value)
            is EditProfileAction.PhotoPicked -> uploadPhotos(listOf(action.uri))
            is EditProfileAction.PhotosPicked -> uploadPhotos(action.uris)
            is EditProfileAction.PhotoRemoved -> _uiState.value = _uiState.value.copy(
                pendingDeletePhotoIds = _uiState.value.pendingDeletePhotoIds + action.photoId,
            )

            is EditProfileAction.PhotoSetAsCover -> {
                val current = _uiState.value.photos
                val target = current.find { it.id == action.photoId } ?: return
                _uiState.value = _uiState.value.copy(
                    photos = listOf(target) + current.filter { it.id != action.photoId },
                )
            }

            is EditProfileAction.PhotoMoved -> {
                val photos = _uiState.value.photos.toMutableList()
                if (action.from in photos.indices && action.to in photos.indices) {
                    photos.add(action.to, photos.removeAt(action.from))
                    _uiState.value = _uiState.value.copy(photos = photos)
                }
            }

            is EditProfileAction.InterestInputChanged -> _uiState.value = _uiState.value.copy(
                interestInput = action.value,
            )

            is EditProfileAction.InterestAdded -> addInterest(action.value)
            is EditProfileAction.InterestRemoved -> _uiState.value = _uiState.value.copy(
                interests = _uiState.value.interests.filterNot { it.equals(action.value, ignoreCase = true) },
            )

            is EditProfileAction.ProvincePickerVisibilityChanged -> _uiState.value = _uiState.value.copy(
                showProvincePicker = action.visible,
            )

            is EditProfileAction.DistrictPickerVisibilityChanged -> _uiState.value = _uiState.value.copy(
                showDistrictPicker = action.visible,
            )

            is EditProfileAction.ProvinceSelected -> {
                _uiState.value = _uiState.value.copy(
                    province = action.value,
                    district = null,
                    districts = emptyList(),
                    showProvincePicker = false,
                )
                loadDistricts(action.value)
            }

            is EditProfileAction.DistrictSelected -> _uiState.value = _uiState.value.copy(
                district = action.value,
                showDistrictPicker = false,
            )

            EditProfileAction.Submit -> submit()
        }
    }

    private fun loadConfig() {
        viewModelScope.launch {
            when (val result = configRepository.getConfig()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(maxPhotos = result.data.maxPhotosPerUser)
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(error = result.error)
            }
        }
    }

    private fun loadProvinces() {
        viewModelScope.launch {
            when (val result = locationRepository.getProvinces()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(provinces = result.data)
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(error = result.error)
            }
        }
    }

    private fun loadDistricts(province: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDistricts = true)
            when (val result = locationRepository.getDistricts(province)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoadingDistricts = false,
                    districts = result.data,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoadingDistricts = false,
                    error = result.error,
                )
            }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            when (val result = profileRepository.getCurrentUser()) {
                is AppResult.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        displayName = user.displayName,
                        bio = user.bio.orEmpty(),
                        gender = user.gender,
                        photos = user.photos.sortedBy { it.displayOrder },
                        interests = user.interests,
                        province = user.province,
                        district = user.district,
                    )
                    user.province?.let { loadDistricts(it) }
                }

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.error,
                )
            }
        }
    }

    private fun uploadPhotos(uris: List<Uri>) {
        if (_uiState.value.isUploadingPhoto) return

        viewModelScope.launch {
            for (uri in uris) {
                if (!_uiState.value.canAddMorePhotos) break

                _uiState.value = _uiState.value.copy(isUploadingPhoto = true, error = null)

                when (val result = photoRepository.uploadPhoto(uri)) {
                    is AppResult.Success -> _uiState.value = _uiState.value.copy(
                        isUploadingPhoto = false,
                        photos = _uiState.value.photos + result.data,
                    )

                    is AppResult.Failure -> {
                        _uiState.value = _uiState.value.copy(isUploadingPhoto = false, error = result.error)
                        break
                    }
                }
            }
        }
    }

    private fun addInterest(value: String) {
        val trimmed = value.trim()
        val state = _uiState.value
        if (trimmed.isEmpty() || !state.canAddMoreInterests) return
        if (state.interests.any { it.equals(trimmed, ignoreCase = true) }) return

        _uiState.value = state.copy(
            interests = state.interests + trimmed,
            interestInput = "",
        )
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, error = null)

            for (photoId in state.pendingDeletePhotoIds) {
                val deleteResult = photoRepository.deletePhoto(photoId)
                if (deleteResult is AppResult.Failure) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = deleteResult.error)
                    return@launch
                }
            }

            if (state.photosReordered) {
                val reorderResult = photoRepository.reorderPhotos(state.visiblePhotos.map { it.id })
                if (reorderResult is AppResult.Failure) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = reorderResult.error)
                    return@launch
                }
            }

            val result = profileRepository.updateProfile(
                displayName = state.displayName,
                bio = state.bio,
                gender = state.gender,
                interests = state.interests,
                province = state.province,
                district = state.district,
            )

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        photos = state.visiblePhotos,
                        pendingDeletePhotoIds = emptySet(),
                    )
                    _events.send(EditProfileEvent.NavigateBack)
                }

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.error,
                )
            }
        }
    }
}
