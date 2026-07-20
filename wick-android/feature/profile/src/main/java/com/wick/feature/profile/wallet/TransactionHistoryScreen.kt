package com.wick.feature.profile.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.ShimmerListRow
import com.wick.core.designsystem.component.WickErrorState
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.profile.R

enum class TxType { PURCHASE, SPEND, BONUS }

data class TokenTransaction(
    val id: String,
    val reason: String,
    val timeLabel: String,
    val amount: Int,
    val type: TxType,
)

data class TxGroup(val label: String, val items: List<TokenTransaction>)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionHistoryRoute(
    onBack: () -> Unit,
    viewModel: TransactionHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionHistoryScreen(onBack = onBack, uiState = uiState, onRetry = viewModel::retry, onRefresh = viewModel::retry, onLoadMore = viewModel::loadMore)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionHistoryScreen(onBack: () -> Unit, uiState: TransactionHistoryUiState, onRetry: () -> Unit, onRefresh: () -> Unit, onLoadMore: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(WickColors.Background)) {
        WickTopBar(title = stringResource(R.string.transaction_history_title), onBackClick = onBack)

        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(8) { ShimmerListRow() }
            }
            return@Column
        }

        if (uiState.hasError) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WickErrorState(
                    message = stringResource(R.string.transaction_history_error),
                    onRetry = onRetry,
                )
            }
            return@Column
        }

        val listState = rememberLazyListState()

        LaunchedEffect(listState) {
            snapshotFlow {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = listState.layoutInfo.totalItemsCount
                lastVisible >= total - 3
            }.collect { nearEnd ->
                if (nearEnd && uiState.hasMore) onLoadMore()
            }
        }

        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item(key = "stats") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label = stringResource(R.string.transaction_history_total_label),
                        value = uiState.totalBalance,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.transaction_history_month_label),
                        value = uiState.monthlyDelta,
                        valueColor = WickColors.Primary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            uiState.groups.forEach { group ->
                item(key = "header_${group.label}") {
                    Text(
                        text = group.label,
                        style = WickTextStyles.Label,
                        color = WickColors.TextMuted,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                    )
                }
                group.items.forEach { tx ->
                    item(key = tx.id) {
                        TransactionRow(tx = tx)
                    }
                }
            }

            if (uiState.isLoadingMore) {
                item(key = "loading_more") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = WickColors.Primary,
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = WickColors.TextPrimary,
) {
    Column(
        modifier = modifier
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = WickTextStyles.BodySmall, color = WickColors.TextMuted)
        Text(text = value, style = WickTextStyles.HeadlineLarge, color = valueColor)
    }
}

@Composable
private fun TransactionRow(tx: TokenTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val (icon, iconTint, iconBg) = when (tx.type) {
            TxType.PURCHASE -> Triple(Icons.Filled.ShoppingCart, WickColors.Primary, WickColors.Primary.copy(alpha = 0.15f))
            TxType.SPEND -> Triple(Icons.Filled.LocalFireDepartment, WickColors.Urgency, WickColors.Urgency.copy(alpha = 0.15f))
            TxType.BONUS -> Triple(Icons.Filled.Add, WickColors.Success, WickColors.Success.copy(alpha = 0.15f))
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(WickShapes.Chip)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }

        val sourceLabel = if (tx.type == TxType.SPEND) stringResource(R.string.transaction_history_source_wick) else stringResource(R.string.transaction_history_source_system)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = tx.reason.toTransactionTitle(), style = WickTextStyles.Label, color = WickColors.TextPrimary)
            Text(text = "${tx.timeLabel} • $sourceLabel", style = WickTextStyles.BodySmall, color = WickColors.TextMuted)
        }

        Text(
            text = if (tx.amount > 0) "+${tx.amount}" else tx.amount.toString(),
            style = WickTextStyles.Label,
            color = if (tx.amount > 0) WickColors.Success else WickColors.Urgency,
        )
    }
}

@Composable
private fun String.toTransactionTitle(): String = when (this) {
    "DAILY_LOGIN" -> stringResource(R.string.transaction_history_reason_daily_login)
    "VERIFICATION_BONUS" -> stringResource(R.string.transaction_history_reason_verification_bonus)
    "PREMIUM_GRANT" -> stringResource(R.string.transaction_history_reason_premium_grant)
    "SESSION_EXTENSION" -> stringResource(R.string.transaction_history_reason_session_extension)
    "EXTRA_INVITATION" -> stringResource(R.string.transaction_history_reason_extra_invitation)
    "PURCHASED" -> stringResource(R.string.transaction_history_reason_purchased)
    else -> stringResource(R.string.transaction_history_reason_unknown)
}
