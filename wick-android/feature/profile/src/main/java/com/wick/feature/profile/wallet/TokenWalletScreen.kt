package com.wick.feature.profile.wallet

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.BillingProduct
import com.wick.core.model.UiError
import com.wick.feature.profile.R

@Composable
fun TokenWalletRoute(onBack: () -> Unit, viewModel: TokenWalletViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    TokenWalletScreen(
        uiState = uiState,
        onBack = onBack,
        onPurchase = viewModel::purchase,
        onClaimDailyBonus = viewModel::claimDailyBonus,
    )
}

@Composable
private fun TokenWalletScreen(
    uiState: TokenWalletUiState,
    onBack: () -> Unit,
    onPurchase: (Activity, String) -> Unit,
    onClaimDailyBonus: () -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar(onBackClick = onBack)

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WickColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "balance") {
                    BalanceHeader(balance = uiState.balance, premium = uiState.premium)
                }

                uiState.error?.let { error ->
                    item(key = "error") {
                        WickInlineWarning(text = error.toDisplayMessage())
                    }
                }

                if (uiState.products.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.token_wallet_no_products),
                            style = WickTextStyles.Body,
                            color = WickColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        )
                    }
                } else {
                    items(uiState.products, key = { it.productId }) { product ->
                        ProductRow(
                            product = product,
                            isPurchasing = uiState.purchasingProductId == product.productId,
                            enabled = uiState.purchasingProductId == null,
                            onClick = { (context as? Activity)?.let { onPurchase(it, product.productId) } },
                        )
                    }
                }

                item(key = "usage") {
                    TokenUsageSection()
                }

                item(key = "daily_bonus") {
                    DailyBonusCard(
                        claimed = uiState.dailyBonusClaimedToday,
                        isLoading = uiState.isClaimingDailyBonus || uiState.isDailyBonusStatusLoading,
                        onClaim = onClaimDailyBonus,
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceHeader(balance: Int, premium: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.token_wallet_balance_label),
            style = WickTextStyles.Label,
            color = WickColors.TextSecondary,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.token_wallet_balance_value, balance),
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
            )
            Icon(imageVector = Icons.Filled.Bolt, contentDescription = null, tint = WickColors.Primary)
        }
        if (premium) {
            Text(
                text = stringResource(R.string.token_wallet_premium_active),
                style = WickTextStyles.Label,
                color = WickColors.Primary,
            )
        }
    }
}

@Composable
private fun ProductRow(product: BillingProduct, isPurchasing: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = product.title, style = WickTextStyles.Label, color = WickColors.TextPrimary)
            if (product.description.isNotBlank()) {
                Text(text = product.description, style = WickTextStyles.BodySmall, color = WickColors.TextSecondary)
            }
        }

        if (isPurchasing) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = WickColors.Primary, strokeWidth = 2.dp)
        } else {
            Text(text = product.formattedPrice, style = WickTextStyles.Label, color = WickColors.Primary)
        }
    }
}

@Composable
private fun TokenUsageSection() {
    val usages = listOf(
        Triple(Icons.Filled.Timer, R.string.token_wallet_usage_extend, R.string.token_wallet_usage_extend_desc),
        Triple(Icons.Filled.LocalFireDepartment, R.string.token_wallet_usage_invite, R.string.token_wallet_usage_invite_desc),
        Triple(Icons.Filled.Person, R.string.token_wallet_usage_boost, R.string.token_wallet_usage_boost_desc),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.token_wallet_usage_title),
            style = WickTextStyles.Label,
            color = WickColors.TextSecondary,
        )
        usages.forEach { (icon, titleRes, descRes) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WickShapes.Card)
                    .background(WickColors.Surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(WickShapes.IconBox)
                        .background(WickColors.Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = WickColors.Primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = stringResource(titleRes), style = WickTextStyles.Label, color = WickColors.TextPrimary)
                    Text(text = stringResource(descRes), style = WickTextStyles.BodySmall, color = WickColors.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun DailyBonusCard(claimed: Boolean, isLoading: Boolean, onClaim: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Primary)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.token_wallet_daily_bonus_title),
            style = WickTextStyles.HeadlineMedium,
            color = WickColors.OnPrimary,
        )
        Text(
            text = stringResource(R.string.token_wallet_daily_bonus_body),
            style = WickTextStyles.Body,
            color = WickColors.OnPrimary,
        )
        WickButton(
            text = if (claimed) {
                stringResource(R.string.token_wallet_daily_bonus_claimed)
            } else {
                stringResource(R.string.token_wallet_daily_bonus_action)
            },
            onClick = onClaim,
            enabled = !claimed && !isLoading,
            loading = isLoading,
            variant = WickButtonVariant.Secondary,
        )
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Unauthorized -> stringResource(R.string.token_wallet_error_unauthorized)
    UiError.NoConnection -> stringResource(R.string.token_wallet_error_no_connection)
    UiError.Conflict -> stringResource(R.string.token_wallet_daily_bonus_already_claimed)
    UiError.ProductNotFound -> stringResource(R.string.token_wallet_error_product_not_found)
    is UiError.Message -> text
    UiError.Forbidden, UiError.RateLimited, UiError.Unknown, UiError.InsufficientTokens,
    UiError.SessionNotFound, UiError.InvalidLink -> stringResource(R.string.token_wallet_error_unknown)
}
