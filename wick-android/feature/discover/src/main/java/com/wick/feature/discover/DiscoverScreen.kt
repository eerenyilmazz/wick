package com.wick.feature.discover

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickBottomNav
import com.wick.core.designsystem.component.WickBottomNavItem
import com.wick.core.designsystem.component.WickErrorState
import com.wick.core.designsystem.component.WickInlineSuccess
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickSwipeableCard
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.component.rememberWickSwipeableCardState
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.model.UiError
import kotlinx.coroutines.launch

@Composable
fun DiscoverRoute(
    onNavigateToProfile: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToConversations: () -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DiscoverScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToMatches = onNavigateToMatches,
        onNavigateToConversations = onNavigateToConversations,
    )
}

@Composable
private fun DiscoverScreen(
    uiState: DiscoverUiState,
    onAction: (DiscoverAction) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToConversations: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val cardState = rememberWickSwipeableCardState(key = uiState.currentUser?.id)
    var showDetail by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.reportedSuccessfully) {
        if (uiState.reportedSuccessfully) {
            delay(3000)
            onAction(DiscoverAction.ClearReportSuccess)
        }
    }

    BackHandler(enabled = showDetail) { showDetail = false }

    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar(
            trailingContent = {
                IconButton(onClick = { onAction(DiscoverAction.OpenFilters) }) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = stringResource(R.string.discover_filter_action),
                        tint = WickColors.Primary,
                    )
                }
            },
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = WickColors.Primary,
                )

                uiState.error != null && uiState.users.isEmpty() -> WickErrorState(
                    message = uiState.error.toDisplayMessage(),
                    onRetry = { onAction(DiscoverAction.Retry) },
                    modifier = Modifier.align(Alignment.Center),
                )

                uiState.currentUser == null -> DiscoverEmptyState(
                    onRetry = { onAction(DiscoverAction.Retry) },
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> WickSwipeableCard(
                    state = cardState,
                    enabled = !uiState.isSwiping,
                    onSwipedLeft = { onAction(DiscoverAction.Pass) },
                    onSwipedRight = { onAction(DiscoverAction.Like) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    DiscoverCard(
                        user = uiState.currentUser!!,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showDetail = true },
                    )
                }
            }
        }

        if (uiState.currentUser != null) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.reportedSuccessfully) {
                    WickInlineSuccess(text = stringResource(R.string.discover_report_success))
                } else {
                    uiState.error?.let { error ->
                        WickInlineWarning(text = error.toDisplayMessage())
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SwipeButton(
                        icon = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.discover_pass),
                        containerColor = WickColors.Surface,
                        iconColor = WickColors.TextSecondary,
                        enabled = !uiState.isSwiping,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                cardState.swipeLeft()
                                onAction(DiscoverAction.Pass)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    SwipeButton(
                        icon = Icons.Filled.StarRate,
                        contentDescription = stringResource(R.string.discover_super_like),
                        containerColor = WickColors.Tertiary,
                        iconColor = WickColors.OnTertiary,
                        enabled = !uiState.isSwiping,
                        badge = stringResource(R.string.discover_super_like_cost, uiState.superLikeTokenCost),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                cardState.swipeRight()
                                onAction(DiscoverAction.SuperLike)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    SwipeButton(
                        icon = Icons.Filled.LocalFireDepartment,
                        contentDescription = stringResource(R.string.discover_like),
                        containerColor = WickColors.Primary,
                        iconColor = WickColors.OnPrimary,
                        enabled = !uiState.isSwiping,
                        glow = true,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                cardState.swipeRight()
                                onAction(DiscoverAction.Like)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        WickBottomNav(
            items = listOf(
                WickBottomNavItem(Icons.Filled.LocalFireDepartment, stringResource(R.string.discover_nav_discover)),
                WickBottomNavItem(Icons.Filled.Favorite, stringResource(R.string.discover_nav_matches)),
                WickBottomNavItem(Icons.Filled.ChatBubbleOutline, stringResource(R.string.discover_nav_messages)),
                WickBottomNavItem(Icons.Filled.Person, stringResource(R.string.discover_nav_profile)),
            ),
            selectedIndex = 0,
            onItemSelected = { index ->
                when (index) {
                    1 -> onNavigateToMatches()
                    2 -> onNavigateToConversations()
                    3 -> onNavigateToProfile()
                    else -> Unit
                }
            },
        )
    }

    uiState.matchedDisplayName?.let { name ->
        MatchDialog(
            displayName = name,
            ownPhotoUrl = uiState.ownPhotoUrl,
            matchedPhotoUrl = uiState.matchedPhotoUrl,
            onSendInvite = { onAction(DiscoverAction.SendInvite) },
            onDismiss = { onAction(DiscoverAction.MatchDismissed) },
        )
    }

    if (uiState.filterSheetVisible) {
        DiscoverFilterSheet(uiState = uiState, onAction = onAction)
    }

    if (showDetail && uiState.currentUser != null) {
        ProfileDetailScreen(
            user = uiState.currentUser!!,
            onBack = { showDetail = false },
            onPass = {
                showDetail = false
                scope.launch {
                    cardState.swipeLeft()
                    onAction(DiscoverAction.Pass)
                }
            },
            onLike = {
                showDetail = false
                scope.launch {
                    cardState.swipeRight()
                    onAction(DiscoverAction.Like)
                }
            },
            onSuperLike = {
                showDetail = false
                scope.launch {
                    cardState.swipeRight()
                    onAction(DiscoverAction.SuperLike)
                }
            },
            onBlock = {
                showDetail = false
                onAction(DiscoverAction.Block(uiState.currentUser!!.id))
            },
            onReport = { reason ->
                showDetail = false
                onAction(DiscoverAction.Report(uiState.currentUser!!.id, reason))
            },
        )
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Unauthorized -> stringResource(R.string.discover_error_unauthorized)
    UiError.NoConnection -> stringResource(R.string.discover_error_no_connection)
    is UiError.Message -> text
    UiError.InsufficientTokens -> stringResource(R.string.discover_error_insufficient_tokens)
    UiError.Conflict, UiError.Forbidden, UiError.RateLimited, UiError.Unknown,
    UiError.SessionNotFound, UiError.InvalidLink, UiError.ProductNotFound ->
        stringResource(R.string.discover_error_unknown)
}
