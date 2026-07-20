package com.wick.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.wick.app.BuildConfig
import com.wick.core.data.auth.AuthRepository
import com.wick.core.data.config.ConfigRepository
import com.wick.core.data.user.ProfileRepository
import com.wick.core.model.AppResult
import com.wick.core.model.UiError
import com.wick.core.model.isProfileComplete
import com.wick.core.network.user.UpdateDeviceTokenRequestDto
import com.wick.core.network.user.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject

sealed interface StartDestination {
    data object Loading : StartDestination
    data object Login : StartDestination
    data object ProfileSetup : StartDestination
    data object Discover : StartDestination
    data object Maintenance : StartDestination
    data object ForceUpdate : StartDestination
}

@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val configRepository: ConfigRepository,
    private val userApi: UserApi,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<StartDestination>(StartDestination.Loading)
    val startDestination: StateFlow<StartDestination> = _startDestination.asStateFlow()

    init {
        resolve()
    }

    private fun resolve() {
        viewModelScope.launch {
            when (val configResult = configRepository.getConfig()) {
                is AppResult.Success -> {
                    val config = configResult.data
                    if (config.maintenanceMode) {
                        _startDestination.value = StartDestination.Maintenance
                        return@launch
                    }
                    if (isVersionOutdated(BuildConfig.VERSION_NAME, config.minVersion)) {
                        _startDestination.value = StartDestination.ForceUpdate
                        return@launch
                    }
                }
                is AppResult.Failure -> {
                    _startDestination.value = if (authRepository.hasSession()) {
                        StartDestination.Discover
                    } else {
                        StartDestination.Login
                    }
                    return@launch
                }
            }

            if (!authRepository.hasSession()) {
                _startDestination.value = StartDestination.Login
                return@launch
            }

            syncFcmToken()

            when (val result = profileRepository.getCurrentUser()) {
                is AppResult.Success -> _startDestination.value = if (result.data.isProfileComplete) {
                    StartDestination.Discover
                } else {
                    StartDestination.ProfileSetup
                }

                is AppResult.Failure -> {
                    if (result.error == UiError.Unauthorized) {
                        authRepository.logout()
                        _startDestination.value = StartDestination.Login
                    } else {
                        _startDestination.value = StartDestination.Discover
                    }
                }
            }
        }
    }

    private suspend fun syncFcmToken() {
        runCatching {
            val token = suspendCancellableCoroutine { cont ->
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            userApi.updateDeviceToken(UpdateDeviceTokenRequestDto(token))
        }.onFailure { Timber.w(it, "FCM token sync failed") }
    }

    private fun isVersionOutdated(current: String, minimum: String): Boolean {
        return try {
            val c = current.split(".").map { it.toInt() }
            val m = minimum.split(".").map { it.toInt() }
            val size = maxOf(c.size, m.size)
            for (i in 0 until size) {
                val cv = c.getOrElse(i) { 0 }
                val mv = m.getOrElse(i) { 0 }
                if (cv < mv) return true
                if (cv > mv) return false
            }
            false
        } catch (_: Exception) {
            false
        }
    }
}
