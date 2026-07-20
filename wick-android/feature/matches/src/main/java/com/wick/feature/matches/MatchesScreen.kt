package com.wick.feature.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
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
import com.wick.core.model.MatchSummary
import com.wick.core.model.UiError
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlinx.coroutines.delay

@Composable
fun MatchesRoute(
    onNavigateToDiscover: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToChat: (matchId: String, otherUserId: String, otherDisplayName: String, otherPhotoUrl: String?) -> Unit,
    viewModel: MatchesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMatch by remember { mutableStateOf<MatchSummary?>(null) }

    val match = selectedMatch
    if (match != null) {
        val invState = uiState.invitationStates[match.id] ?: InvitationSendState.Idle
        val invitationId = (invState as? InvitationSendState.Sent)?.invitation?.id
        MatchDetailScreen(
            match = match,
            invitationState = invState,
            isUnmatching = match.id in uiState.unmatchingMatchIds,
            isCancelling = invitationId != null && invitationId in uiState.cancellingInvitationIds,
            retryTokenCost = uiState.extraInvitationTokenCost,
            retryLimitReached = invState is InvitationSendState.Failed && invState.error == com.wick.core.model.UiError.RateLimited,
            onBack = { selectedMatch = null },
            onInvite = { viewModel.sendInvitation(match.id) },
            onRetryInvite = { viewModel.sendInvitation(match.id, useToken = true) },
            onCancelInvitation = { invitationId?.let { viewModel.cancelInvitation(it, match.id) } },
            onOpenChat = {
                onNavigateToChat(match.id, match.otherUser.id, match.otherUser.displayName, match.otherUser.photos.firstOrNull()?.url)
            },
            onUnmatch = { viewModel.unmatch(match.id) { selectedMatch = null } },
        )
    } else {
        MatchesScreen(
            uiState = uiState,
            onRetry = viewModel::retry,
            onMatchClick = { selectedMatch = it },
            onAcceptInvitation = viewModel::acceptInvitation,
            onDeclineInvitation = viewModel::declineInvitation,
            onNavigateToDiscover = onNavigateToDiscover,
            onNavigateToConversations = onNavigateToConversations,
            onNavigateToProfile = onNavigateToProfile,
            onConfirmPaidInvite = viewModel::confirmPaidInvite,
            onDismissPaidInviteSheet = viewModel::dismissPaidInviteSheet,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchesScreen(
    uiState: MatchesUiState,
    onRetry: () -> Unit,
    onMatchClick: (MatchSummary) -> Unit,
    onAcceptInvitation: (String) -> Unit,
    onDeclineInvitation: (String) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onConfirmPaidInvite: (String) -> Unit,
    onDismissPaidInviteSheet: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar()

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = WickColors.Primary,
                )

                uiState.error != null && uiState.matches.isEmpty() -> WickErrorState(
                    message = uiState.error.toDisplayMessage(),
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )

                uiState.matches.isEmpty() -> MatchesEmptyState(
                    onNavigateToDiscover = onNavigateToDiscover,
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = onRetry,
                    modifier = Modifier.fillMaxSize(),
                ) { LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.incomingInvitations.isNotEmpty()) {
                        item(key = "incoming_invitations") {
                            IncomingInvitationsSection(
                                invitations = uiState.incomingInvitations,
                                respondingInvitationIds = uiState.respondingInvitationIds,
                                onAccept = onAcceptInvitation,
                                onDecline = onDeclineInvitation,
                            )
                        }
                    }

                    val incomingMatchIds = uiState.incomingInvitations.map { it.invitation.matchId }.toSet()
                    items(uiState.matches.filterNot { it.id in incomingMatchIds }, key = { it.id }) { match ->
                        MatchRow(
                            match = match,
                            invitationState = uiState.invitationStates[match.id] ?: InvitationSendState.Idle,
                            sessionStatus = match.sessionStatus,
                            onClick = { onMatchClick(match) },
                            onInvite = { onMatchClick(match) },
                        )
                    }
                } }
            }
        }

        WickBottomNav(
            items = listOf(
                WickBottomNavItem(Icons.Filled.LocalFireDepartment, stringResource(R.string.matches_nav_discover)),
                WickBottomNavItem(Icons.Filled.Favorite, stringResource(R.string.matches_nav_matches)),
                WickBottomNavItem(Icons.Filled.ChatBubbleOutline, stringResource(R.string.matches_nav_conversations)),
                WickBottomNavItem(Icons.Filled.Person, stringResource(R.string.matches_nav_profile)),
            ),
            selectedIndex = 1,
            onItemSelected = { index ->
                when (index) {
                    0 -> onNavigateToDiscover()
                    2 -> onNavigateToConversations()
                    3 -> onNavigateToProfile()
                    else -> Unit
                }
            },
        )
    }

    val paidMatchId = uiState.pendingPaidInviteMatchId
    if (paidMatchId != null) {
        val paidMatch = uiState.matches.find { it.id == paidMatchId }
        if (paidMatch != null) {
            PaidInviteSheet(
                match = paidMatch,
                tokenCost = uiState.extraInvitationTokenCost,
                onConfirm = { onConfirmPaidInvite(paidMatchId) },
                onDismiss = onDismissPaidInviteSheet,
            )
        }
    }
    }
}

@Composable
private fun PaidInviteSheet(
    match: MatchSummary,
    tokenCost: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WickColors.Background.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(WickShapes.Sheet)
                .background(WickColors.Surface)
                .clickable(enabled = false, onClick = {})
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(WickColors.Border),
            )

            Text(
                text = stringResource(R.string.matches_paid_invite_title),
                style = WickTextStyles.HeadlineMedium,
                color = WickColors.TextPrimary,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                MatchAvatar(
                    name = match.otherUser.displayName,
                    photoUrl = match.otherUser.photos.minByOrNull { it.displayOrder }?.url,
                )
                Text(
                    text = stringResource(R.string.matches_paid_invite_heading, match.otherUser.displayName),
                    style = WickTextStyles.HeadlineMedium,
                    color = WickColors.TextPrimary,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WickShapes.Card)
                    .background(WickColors.Background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.matches_paid_invite_body, tokenCost),
                    style = WickTextStyles.Body,
                    color = WickColors.TextSecondary,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WickShapes.Button)
                    .background(WickColors.Primary)
                    .clickable(onClick = onConfirm)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.matches_paid_invite_cta, tokenCost),
                    style = WickTextStyles.Label,
                    color = WickColors.OnPrimary,
                )
            }

            Text(
                text = stringResource(R.string.matches_paid_invite_cancel),
                style = WickTextStyles.Body,
                color = WickColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(WickShapes.Chip)
                    .clickable(onClick = onDismiss)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun MatchesEmptyState(onNavigateToDiscover: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Primary.copy(alpha = 0.12f))
                .border(2.dp, WickColors.Primary.copy(alpha = 0.3f), WickShapes.IconBox),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
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
                text = stringResource(R.string.matches_empty_title),
                style = WickTextStyles.HeadlineMedium,
                color = WickColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.matches_empty_body),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        WickButton(
            text = stringResource(R.string.matches_empty_cta),
            onClick = onNavigateToDiscover,
            variant = WickButtonVariant.Secondary,
            modifier = Modifier.wrapContentWidth(),
        )
    }
}

@Composable
private fun MatchRow(
    match: MatchSummary,
    invitationState: InvitationSendState,
    sessionStatus: String?,
    onClick: () -> Unit,
    onInvite: () -> Unit,
) {
    val isDeclined = invitationState is InvitationSendState.Declined
    val isIncoming = invitationState is InvitationSendState.IncomingPending
    val isSessionLocked = sessionStatus == "LOCKED"
    val isSessionActive = sessionStatus == "ACTIVE"

    val sessionRemainingSeconds by produceState(
        initialValue = if (isSessionActive) runCatching {
            ChronoUnit.SECONDS.between(Instant.now(), Instant.parse(match.sessionEndsAt)).coerceAtLeast(0)
        }.getOrDefault(0L) else null,
        key1 = match.sessionEndsAt,
        key2 = isSessionActive,
    ) {
        while (isSessionActive && (value ?: 0L) > 0L) {
            delay(1000)
            value = runCatching {
                ChronoUnit.SECONDS.between(Instant.now(), Instant.parse(match.sessionEndsAt)).coerceAtLeast(0)
            }.getOrDefault(0L)
        }
    }
    val rowAlpha = if (isDeclined) 0.5f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(if (isIncoming) WickColors.Primary.copy(alpha = 0.08f) else WickColors.Surface)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .graphicsLayer { alpha = rowAlpha },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MatchAvatar(
            name = match.otherUser.displayName,
            photoUrl = match.otherUser.photos.minByOrNull { it.displayOrder }?.url,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = match.otherUser.displayName,
                style = WickTextStyles.Label,
                color = WickColors.TextPrimary,
            )
            invitationState.toRowStatusLabel(sessionLocked = isSessionLocked)?.let { label ->
                Text(
                    text = label,
                    style = WickTextStyles.BodySmall,
                    color = if (isIncoming) WickColors.Primary else WickColors.TextSecondary,
                )
            }
        }
        val secs = sessionRemainingSeconds
        if (isSessionActive && secs != null) {
            val isUrgent = secs in 1..300
            InvitePill(
                label = "%02d:%02d".format(secs / 60, secs % 60),
                containerColor = if (isUrgent) WickColors.Urgency else WickColors.Primary,
                textColor = WickColors.OnPrimary,
                onClick = null,
            )
        } else if (isSessionLocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = WickColors.TextMuted,
                modifier = Modifier.size(18.dp),
            )
        } else {
            when (invitationState) {
                is InvitationSendState.Idle -> InvitePill(
                    label = stringResource(R.string.matches_row_invite),
                    containerColor = WickColors.Primary,
                    textColor = WickColors.OnPrimary,
                    onClick = onInvite,
                )
                is InvitationSendState.Sending -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = WickColors.Primary,
                    strokeWidth = 2.dp,
                )
                is InvitationSendState.Sent -> InvitePill(
                    label = stringResource(R.string.matches_row_waiting),
                    containerColor = WickColors.Surface,
                    textColor = WickColors.TextSecondary,
                    onClick = null,
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun MatchAvatar(name: String, photoUrl: String?) {
    val avatarColors = listOf(
        androidx.compose.ui.graphics.Color(0xFFFF5A36),
        androidx.compose.ui.graphics.Color(0xFF5FD6F1),
        androidx.compose.ui.graphics.Color(0xFF9C6FD6),
        androidx.compose.ui.graphics.Color(0xFF4CAF6D),
        androidx.compose.ui.graphics.Color(0xFFE8283C),
        androidx.compose.ui.graphics.Color(0xFFFF9B3D),
    )
    val bgColor = avatarColors[abs(name.hashCode()) % avatarColors.size]
    val initial = name.firstOrNull()?.uppercase().orEmpty()

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl != null) {
            WickAsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = initial,
                style = WickTextStyles.HeadlineMedium,
                color = WickColors.OnPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InvitePill(
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)?,
) {
    val base = Modifier
        .clip(WickShapes.Button)
        .background(containerColor)
        .padding(horizontal = 12.dp, vertical = 6.dp)
    Box(
        modifier = if (onClick != null) base.clickable(onClick = onClick) else base,
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = WickTextStyles.Label, color = textColor)
    }
}

@Composable
private fun InvitationSendState.toRowStatusLabel(sessionLocked: Boolean = false): String? = when {
    this is InvitationSendState.Accepted && sessionLocked -> stringResource(R.string.matches_row_status_session_locked)
    this is InvitationSendState.Sent -> stringResource(R.string.matches_row_status_sent)
    this is InvitationSendState.IncomingPending -> stringResource(R.string.matches_row_status_incoming)
    this is InvitationSendState.Accepted -> stringResource(R.string.matches_row_status_accepted)
    this is InvitationSendState.Declined -> stringResource(R.string.matches_row_status_declined)
    else -> null
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Unauthorized -> stringResource(R.string.matches_error_unauthorized)
    UiError.NoConnection -> stringResource(R.string.matches_error_no_connection)
    is UiError.Message -> text
    UiError.Conflict, UiError.Forbidden, UiError.RateLimited, UiError.Unknown,
    UiError.SessionNotFound, UiError.InvalidLink, UiError.ProductNotFound, UiError.InsufficientTokens ->
        stringResource(R.string.matches_error_unknown)
}
