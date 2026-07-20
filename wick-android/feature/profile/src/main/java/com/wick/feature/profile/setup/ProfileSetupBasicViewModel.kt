package com.wick.feature.profile.setup

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
class ProfileSetupBasicViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val photoRepository: PhotoRepository,
    private val profileRepository: ProfileRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupBasicUiState())
    val uiState: StateFlow<ProfileSetupBasicUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileSetupBasicEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadConfig()
        loadPhotos()
        loadProvinces()
    }

    fun onAction(action: ProfileSetupBasicAction) {
        when (action) {
            is ProfileSetupBasicAction.DateOfBirthChanged -> _uiState.value = _uiState.value.copy(
                dateOfBirth = action.value,
                showDatePicker = false,
            )

            is ProfileSetupBasicAction.GenderChanged -> _uiState.value = _uiState.value.copy(gender = action.value)

            is ProfileSetupBasicAction.DatePickerVisibilityChanged -> _uiState.value = _uiState.value.copy(
                showDatePicker = action.visible,
            )

            is ProfileSetupBasicAction.PhotoPicked -> uploadPhotos(listOf(action.uri))
            is ProfileSetupBasicAction.PhotosPicked -> uploadPhotos(action.uris)
            is ProfileSetupBasicAction.PhotoRemoved -> removePhoto(action.photoId)
            is ProfileSetupBasicAction.PhotoMoved -> movePhoto(action.from, action.to)

            is ProfileSetupBasicAction.InterestInputChanged -> _uiState.value = _uiState.value.copy(
                interestInput = action.value,
            )

            is ProfileSetupBasicAction.InterestAdded -> addInterest(action.value)
            is ProfileSetupBasicAction.InterestRemoved -> _uiState.value = _uiState.value.copy(
                interests = _uiState.value.interests.filterNot { it.equals(action.value, ignoreCase = true) },
            )

            is ProfileSetupBasicAction.ProvincePickerVisibilityChanged -> _uiState.value = _uiState.value.copy(
                showProvincePicker = action.visible,
            )

            is ProfileSetupBasicAction.DistrictPickerVisibilityChanged -> _uiState.value = _uiState.value.copy(
                showDistrictPicker = action.visible,
            )

            is ProfileSetupBasicAction.ProvinceSelected -> {
                _uiState.value = _uiState.value.copy(
                    province = action.value,
                    district = null,
                    districts = emptyList(),
                    showProvincePicker = false,
                )
                loadDistricts(action.value)
            }

            is ProfileSetupBasicAction.DistrictSelected -> _uiState.value = _uiState.value.copy(
                district = action.value,
                showDistrictPicker = false,
            )

            ProfileSetupBasicAction.Submit -> submit()
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

    private fun loadConfig() {
        viewModelScope.launch {
            when (val result = configRepository.getConfig()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoadingConfig = false,
                    minimumAge = result.data.minimumAge,
                    maximumAge = result.data.maximumAge,
                    maxPhotos = result.data.maxPhotosPerUser,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoadingConfig = false,
                    error = result.error,
                )
            }
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            when (val result = photoRepository.getPhotos()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoadingPhotos = false,
                    photos = result.data,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoadingPhotos = false,
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

    private fun movePhoto(from: Int, to: Int) {
        val photos = _uiState.value.photos.toMutableList()
        if (from !in photos.indices || to !in photos.indices) return
        photos.add(to, photos.removeAt(from))
        _uiState.value = _uiState.value.copy(photos = photos)
    }

    private fun removePhoto(photoId: String) {
        viewModelScope.launch {
            when (val result = photoRepository.deletePhoto(photoId)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    photos = _uiState.value.photos.filterNot { it.id == photoId },
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(error = result.error)
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val dateOfBirth = state.dateOfBirth ?: return
        val gender = state.gender ?: return
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, error = null)

            val photosReordered = state.photos.map { it.id } != state.photos.sortedBy { it.displayOrder }.map { it.id }
            if (photosReordered && state.photos.size > 1) {
                val reorderResult = photoRepository.reorderPhotos(state.photos.map { it.id })
                if (reorderResult is AppResult.Failure) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = reorderResult.error)
                    return@launch
                }
            }

            val result = profileRepository.updateProfile(
                dateOfBirth = dateOfBirth,
                gender = gender,
                interests = state.interests,
                province = state.province,
                district = state.district,
            )

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    _events.send(ProfileSetupBasicEvent.NavigateToBio)
                }

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.error,
                )
            }
        }
    }
}
