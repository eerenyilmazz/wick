package com.wick.feature.profile.wallet

import androidx.compose.runtime.Immutable
import com.wick.core.model.BillingProduct
import com.wick.core.model.UiError

@Immutable
data class TokenWalletUiState(
    val isLoading: Boolean = true,
    val isDailyBonusStatusLoading: Boolean = true,
    val balance: Int = 0,
    val premium: Boolean = false,
    val products: List<BillingProduct> = emptyList(),
    val purchasingProductId: String? = null,
    val isClaimingDailyBonus: Boolean = false,
    val dailyBonusClaimedToday: Boolean = false,
    val error: UiError? = null,
)
