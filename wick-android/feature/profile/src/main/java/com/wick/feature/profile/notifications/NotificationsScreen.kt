package com.wick.feature.profile.notifications

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.shape.CircleShape
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.ShimmerListRow
import com.wick.core.designsystem.component.WickErrorState
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.InboxNotification
import com.wick.feature.profile.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NotificationsRoute(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMatches: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NotificationsScreen(
        state = state,
        onBack = onBack,
        onMarkAllRead = viewModel::markAllRead,
        onMarkRead = viewModel::markRead,
        onRetry = viewModel::retry,
        onRefresh = viewModel::retry,
        onNavigateToSettings = onNavigateToSettings,
        onLoadMore = viewModel::loadMore,
        onNavigateToMatches = onNavigateToMatches,
        onNavigateToProfile = onNavigateToProfile,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsScreen(
    state: NotificationsUiState,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onMarkRead: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WickColors.Background),
    ) {
        WickTopBar(
            title = stringResource(R.string.notifications_title),
            onBackClick = onBack,
            trailingContent = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = WickColors.Primary,
                    )
                }
            },
        )

        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(6) { ShimmerListRow() }
            }
            return@Column
        }

        if (state.hasError) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WickErrorState(
                    message = stringResource(R.string.notifications_error),
                    onRetry = onRetry,
                )
            }
            return@Column
        }

        if (state.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.notifications_empty),
                    style = WickTextStyles.Body,
                    color = WickColors.TextMuted,
                )
            }
            return@Column
        }

        val todayLabel = stringResource(R.string.notifications_date_today)
        val yesterdayLabel = stringResource(R.string.notifications_date_yesterday)

        val listState = rememberLazyListState()

        LaunchedEffect(listState) {
            snapshotFlow {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = listState.layoutInfo.totalItemsCount
                lastVisible >= total - 3
            }.collect { nearEnd ->
                if (nearEnd && state.hasMore) onLoadMore()
            }
        }

        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "toolbar") {
                val hasUnread = state.items.any { !it.read }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (hasUnread) {
                        Text(
                            text = stringResource(R.string.notifications_mark_all_read),
                            style = WickTextStyles.BodySmall,
                            color = WickColors.Primary,
                            modifier = Modifier
                                .clip(WickShapes.Chip)
                                .clickable(onClick = onMarkAllRead)
                                .padding(4.dp),
                        )
                    }
                }
            }

            val grouped = state.items.groupBy { it.notificationDate() }
            grouped.entries.sortedByDescending { it.key }.forEach { (date, groupItems) ->
                item(key = "date_$date") {
                    Text(
                        text = date.toDisplayHeader(todayLabel, yesterdayLabel),
                        style = WickTextStyles.BodySmall,
                        color = WickColors.TextMuted,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                    )
                }
                items(groupItems, key = { it.id }) { item ->
                    NotificationRow(
                        item = item,
                        onClick = {
                            onMarkRead(item.id)
                            when {
                                item.type.startsWith("VERIFICATION") -> onNavigateToProfile()
                                else -> onNavigateToMatches()
                            }
                        },
                    )
                }
            }

            if (state.isLoadingMore) {
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
private fun NotificationRow(item: InboxNotification, onClick: () -> Unit) {
    val (icon, iconTint, iconBg) = iconFor(item.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(WickShapes.Chip)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.title,
                style = WickTextStyles.Body,
                color = if (!item.read) WickColors.TextPrimary else WickColors.TextSecondary,
            )
            item.body?.let { body ->
                Text(text = body, style = WickTextStyles.BodySmall, color = WickColors.TextMuted)
            }
            Text(
                text = item.notificationTime(),
                style = WickTextStyles.BodySmall,
                color = WickColors.TextMuted,
            )
        }

        if (!item.read) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(WickColors.Primary),
            )
        }
    }
}

private fun InboxNotification.notificationDate(): LocalDate = runCatching {
    Instant.parse(createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
}.getOrDefault(LocalDate.now())

private fun InboxNotification.notificationTime(): String = runCatching {
    val time = Instant.parse(createdAt).atZone(ZoneId.systemDefault())
    "%02d:%02d".format(time.hour, time.minute)
}.getOrDefault("")

private fun LocalDate.toDisplayHeader(todayLabel: String, yesterdayLabel: String): String {
    val today = LocalDate.now()
    return when (this) {
        today -> todayLabel
        today.minusDays(1) -> yesterdayLabel
        else -> {
            val months = listOf("", "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
                "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
            "$dayOfMonth ${months[monthValue]}"
        }
    }
}

private fun iconFor(type: String): Triple<ImageVector, Color, Color> = when (type) {
    "MATCH" -> Triple(Icons.Filled.Favorite, WickColors.Like, WickColors.Like.copy(alpha = 0.15f))
    "INVITE" -> Triple(Icons.Filled.LocalFireDepartment, WickColors.Primary, WickColors.Primary.copy(alpha = 0.15f))
    "SESSION_REMINDER" -> Triple(Icons.Filled.Bolt, WickColors.Urgency, WickColors.Urgency.copy(alpha = 0.15f))
    else -> Triple(Icons.Filled.Notifications, WickColors.TextSecondary, WickColors.TextSecondary.copy(alpha = 0.15f))
}
