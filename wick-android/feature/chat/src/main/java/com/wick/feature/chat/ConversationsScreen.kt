package com.wick.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.ShimmerListRow
import com.wick.core.designsystem.component.WickAsyncImage
import com.wick.core.designsystem.component.WickBottomNav
import com.wick.core.designsystem.component.WickBottomNavItem
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.component.WickErrorState
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.ConversationSummary
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.abs

private val avatarPalette = listOf(
    WickColors.Primary,
    WickColors.Tertiary,
    WickColors.AvatarPurple,
    WickColors.Success,
    WickColors.Urgency,
    WickColors.AvatarOrange,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConversationsRoute(
    onNavigateToDiscover: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: (matchId: String, otherUserId: String, otherDisplayName: String, otherPhotoUrl: String?) -> Unit,
    viewModel: ConversationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ConversationsScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        onRefresh = viewModel::retry,
        onConversationClick = { conv ->
            onNavigateToChat(conv.matchId, conv.otherUserId, conv.otherDisplayName, conv.otherPhotoUrl)
        },
        onNavigateToDiscover = onNavigateToDiscover,
        onNavigateToMatches = onNavigateToMatches,
        onNavigateToProfile = onNavigateToProfile,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationsScreen(
    uiState: ConversationsUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onConversationClick: (ConversationSummary) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar()

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.isLoading -> Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    repeat(6) { ShimmerListRow() }
                }

                uiState.error != null -> WickErrorState(
                    message = stringResource(R.string.conversations_error_unknown),
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )

                uiState.conversations.isEmpty() -> ConversationsEmptyState(
                    onNavigateToMatches = onNavigateToMatches,
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> {
                    val active = uiState.conversations.filter { it.status != "LOCKED" }
                    val past = uiState.conversations.filter { it.status == "LOCKED" }
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(active, key = { it.sessionId }) { conv ->
                                ConversationRow(conversation = conv, onClick = { onConversationClick(conv) })
                            }
                            if (past.isNotEmpty()) {
                                item(key = "past_header") {
                                    Text(
                                        text = stringResource(R.string.conversations_past_header),
                                        style = WickTextStyles.Label,
                                        color = WickColors.TextMuted,
                                        modifier = Modifier.padding(top = if (active.isNotEmpty()) 8.dp else 0.dp, bottom = 2.dp),
                                    )
                                }
                                items(past, key = { it.sessionId }) { conv ->
                                    ConversationRow(conversation = conv, onClick = { onConversationClick(conv) })
                                }
                            }
                        }
                    }
                }
            }
        }

        WickBottomNav(
            items = listOf(
                WickBottomNavItem(Icons.Filled.LocalFireDepartment, stringResource(R.string.conversations_nav_discover)),
                WickBottomNavItem(Icons.Filled.Favorite, stringResource(R.string.conversations_nav_matches)),
                WickBottomNavItem(Icons.Filled.ChatBubbleOutline, stringResource(R.string.conversations_nav_conversations)),
                WickBottomNavItem(Icons.Filled.Person, stringResource(R.string.conversations_nav_profile)),
            ),
            selectedIndex = 2,
            onItemSelected = { index ->
                when (index) {
                    0 -> onNavigateToDiscover()
                    1 -> onNavigateToMatches()
                    3 -> onNavigateToProfile()
                    else -> Unit
                }
            },
        )
    }
}

@Composable
private fun ConversationRow(conversation: ConversationSummary, onClick: () -> Unit) {
    val isExpired = conversation.status == "LOCKED"
    val remainingSeconds by produceState(
        initialValue = if (isExpired) 0L else runCatching {
            ChronoUnit.SECONDS.between(Instant.now(), Instant.parse(conversation.endsAt)).coerceAtLeast(0)
        }.getOrDefault(0L),
        key1 = conversation.endsAt,
        key2 = isExpired,
    ) {
        while (!isExpired && value > 0) {
            delay(1000)
            value = runCatching {
                ChronoUnit.SECONDS.between(Instant.now(), Instant.parse(conversation.endsAt)).coerceAtLeast(0)
            }.getOrDefault(0L)
        }
    }
    val remainingMinutes = remainingSeconds / 60
    val remainingSecs = remainingSeconds % 60
    val isUrgent = remainingSeconds in 1..300

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(if (isExpired) WickColors.Surface.copy(alpha = 0.5f) else WickColors.Surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InitialAvatar(
            name = conversation.otherDisplayName,
            photoUrl = conversation.otherPhotoUrl,
            size = 56,
            faded = isExpired,
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = conversation.otherDisplayName,
                style = WickTextStyles.Label,
                color = if (isExpired) WickColors.TextMuted else WickColors.TextPrimary,
            )
            Text(
                text = if (isExpired) {
                    stringResource(R.string.conversations_expired)
                } else {
                    conversation.lastMessage ?: stringResource(R.string.conversations_no_message_yet)
                },
                style = WickTextStyles.BodySmall,
                color = WickColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (isExpired) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = WickColors.TextMuted,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(WickShapes.Chip)
                    .background(if (isUrgent) WickColors.Urgency else WickColors.Primary)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "%02d:%02d".format(remainingMinutes, remainingSecs),
                    style = WickTextStyles.Label,
                    color = WickColors.OnPrimary,
                )
            }
        }
    }
}

@Composable
internal fun InitialAvatar(
    name: String,
    photoUrl: String?,
    size: Int,
    faded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val color = if (faded) WickColors.TextMuted.copy(alpha = 0.4f)
    else avatarPalette[abs(name.hashCode()) % avatarPalette.size]
    val initial = name.firstOrNull()?.uppercase().orEmpty()

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl != null) {
            WickAsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (faded) Modifier.graphicsLayer { alpha = 0.4f } else Modifier),
            )
        } else {
            Text(
                text = initial,
                style = WickTextStyles.HeadlineMedium,
                color = if (faded) WickColors.TextMuted else WickColors.OnPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ConversationsEmptyState(onNavigateToMatches: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.ChatBubbleOutline,
                contentDescription = null,
                tint = WickColors.Primary,
                modifier = Modifier.size(40.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.conversations_empty_title),
                style = WickTextStyles.HeadlineMedium,
                color = WickColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.conversations_empty_body),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        WickButton(
            text = stringResource(R.string.conversations_empty_cta),
            onClick = onNavigateToMatches,
            variant = WickButtonVariant.Secondary,
            modifier = Modifier.wrapContentWidth(),
        )
    }
}
