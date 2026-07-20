package com.wick.app.navigation

import androidx.lifecycle.ViewModel
import com.wick.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SessionExpiryViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val sessionExpiredFlow: Flow<Unit> = authRepository.sessionExpiredFlow
}
