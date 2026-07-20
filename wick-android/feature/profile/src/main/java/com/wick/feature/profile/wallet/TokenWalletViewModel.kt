package com.wick.feature.profile.wallet

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.payment.PaymentRepository
import com.wick.core.data.token.TokenRepository
import com.wick.core.data.user.ProfileRepository
import com.wick.core.model.AppResult
import com.wick.core.model.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenWalletViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val paymentRepository: PaymentRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TokenWalletUiState())
    val uiState: StateFlow<TokenWalletUiState> = _uiState.asStateFlow()

    init {
        loadBalance()
        loadPremiumStatus()
        loadProducts()
        observePurchases()
        checkDailyBonusStatus()
    }

    fun refresh() {
        loadBalance()
        checkDailyBonusStatus()
    }

    fun purchase(activity: Activity, productId: String) {
        if (_uiState.value.purchasingProductId != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(purchasingProductId = productId, error = null)
            val result = paymentRepository.launchPurchase(activity, productId)
            if (result is AppResult.Failure) {
                _uiState.value = _uiState.value.copy(purchasingProductId = null, error = result.error)
            }
        }
    }

    fun claimDailyBonus() {
        if (_uiState.value.isClaimingDailyBonus) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isClaimingDailyBonus = true, error = null)
            when (val result = tokenRepository.claimDailyLogin()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isClaimingDailyBonus = false,
                    dailyBonusClaimedToday = true,
                    balance = result.data,
                )

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isClaimingDailyBonus = false,
                    dailyBonusClaimedToday = result.error == UiError.Conflict,
                    error = result.error,
                )
            }
        }
    }

    private fun checkDailyBonusStatus() {
        viewModelScope.launch {
            when (val result = tokenRepository.getDailyLoginStatus()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isDailyBonusStatusLoading = false,
                    dailyBonusClaimedToday = result.data,
                )
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(isDailyBonusStatusLoading = false)
            }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            when (val result = tokenRepository.getBalance()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, balance = result.data)
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.error)
            }
        }
    }

    private fun loadPremiumStatus() {
        viewModelScope.launch {
            val result = profileRepository.getCurrentUser()
            if (result is AppResult.Success) {
                _uiState.value = _uiState.value.copy(premium = result.data.premium)
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            when (val result = paymentRepository.getProductCatalog()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(products = result.data)
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(error = result.error)
            }
        }
    }

    private fun observePurchases() {
        viewModelScope.launch {
            paymentRepository.observeCompletedPurchases().collect { result ->
                when (result) {
                    is AppResult.Success -> _uiState.value = _uiState.value.copy(
                        purchasingProductId = null,
                        balance = result.data.tokenBalance,
                        premium = result.data.premium || _uiState.value.premium,
                    )

                    is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                        purchasingProductId = null,
                        error = result.error,
                    )
                }
            }
        }
    }
}
