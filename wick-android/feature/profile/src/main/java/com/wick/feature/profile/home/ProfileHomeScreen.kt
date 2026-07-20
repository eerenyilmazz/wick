package com.wick.feature.profile.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.wick.core.designsystem.component.WickBottomNav
import com.wick.core.designsystem.component.WickBottomNavItem
import com.wick.core.designsystem.component.WickVerifiedBadge
import com.wick.core.designsystem.component.WickInlineSuccess
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.profile.R

@Composable
fun ProfileHomeRoute(
    onNavigateToDiscover: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToLegalDocuments: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPreview: () -> Unit,
    onNavigateToTokenWallet: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTransactionHistory: () -> Unit = {},
    onNavigateToNotificationPreferences: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAccountSecurity: () -> Unit = {},
    onNavigateToAccountVerification: () -> Unit = {},
    viewModel: ProfileHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileHomeEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    ProfileHomeScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToDiscover = onNavigateToDiscover,
        onNavigateToMatches = onNavigateToMatches,
        onNavigateToConversations = onNavigateToConversations,
        onNavigateToLegalDocuments = onNavigateToLegalDocuments,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onNavigateToPreview = onNavigateToPreview,
        onNavigateToTokenWallet = onNavigateToTokenWallet,
        onNavigateToTransactionHistory = onNavigateToTransactionHistory,
        onNavigateToNotificationPreferences = onNavigateToNotificationPreferences,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToAccountSecurity = onNavigateToAccountSecurity,
        onNavigateToAccountVerification = onNavigateToAccountVerification,
    )
}

@Composable
private fun ProfileHomeScreen(
    uiState: ProfileHomeUiState,
    onAction: (ProfileHomeAction) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToLegalDocuments: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPreview: () -> Unit,
    onNavigateToTokenWallet: () -> Unit,
    onNavigateToTransactionHistory: () -> Unit,
    onNavigateToNotificationPreferences: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAccountSecurity: () -> Unit,
    onNavigateToAccountVerification: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar()

        if (uiState.exportSuccess) {
            WickInlineSuccess(
                text = stringResource(R.string.profile_home_export_success),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = WickColors.Primary,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PhotoFan(
                        photos = uiState.user?.photos.orEmpty().sortedBy { it.displayOrder },
                        fallbackInitial = uiState.user?.displayName?.firstOrNull()?.uppercase().orEmpty(),
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .clickable(onClick = onNavigateToPreview),
                    )
                    Text(
                        text = stringResource(R.string.profile_home_preview_hint),
                        style = WickTextStyles.BodySmall,
                        color = WickColors.Primary,
                        modifier = Modifier.clickable(onClick = onNavigateToPreview),
                    )

                    if (uiState.user?.premium == true) {
                        Text(
                            text = stringResource(R.string.profile_home_premium_badge),
                            style = WickTextStyles.Label,
                            color = WickColors.OnPrimary,
                            modifier = Modifier
                                .clip(WickShapes.Chip)
                                .background(WickColors.Primary)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }

                    when (uiState.user?.verificationStatus) {
                        "VERIFIED" -> WickVerifiedBadge(contentDescription = stringResource(R.string.profile_home_verified_badge))
                        "PENDING" -> Text(
                            text = stringResource(R.string.profile_home_verification_pending),
                            style = WickTextStyles.BodySmall,
                            color = WickColors.TextMuted,
                            modifier = Modifier
                                .clip(WickShapes.Chip)
                                .background(WickColors.Surface)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                        "REJECTED" -> Text(
                            text = stringResource(R.string.profile_home_verification_rejected),
                            style = WickTextStyles.BodySmall,
                            color = WickColors.Urgency,
                            modifier = Modifier
                                .clip(WickShapes.Chip)
                                .background(WickColors.Surface)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                        else -> Unit
                    }

                    Text(
                        text = uiState.user?.displayName.orEmpty(),
                        style = WickTextStyles.HeadlineLarge,
                        color = WickColors.TextPrimary,
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 16.dp)
                            .clickable(onClick = onNavigateToPreview),
                    )

                    ProfileSection(title = stringResource(R.string.profile_home_section_account)) {
                        ProfileRow(
                            icon = Icons.Filled.Edit,
                            label = stringResource(R.string.profile_home_edit_profile),
                            onClick = onNavigateToEditProfile,
                        )
                        val verificationStatus = uiState.user?.verificationStatus
                        val verificationBlocked = verificationStatus == "VERIFIED" || verificationStatus == "PENDING"
                        ProfileRow(
                            icon = Icons.Filled.Shield,
                            label = when (verificationStatus) {
                                "VERIFIED" -> stringResource(R.string.profile_home_verified_badge)
                                "PENDING" -> stringResource(R.string.profile_home_verification_pending)
                                else -> stringResource(R.string.profile_home_verify_account)
                            },
                            onClick = { if (!verificationBlocked) onNavigateToAccountVerification() },
                            enabled = !verificationBlocked,
                        )
                    }

                    ProfileSection(title = stringResource(R.string.profile_home_section_wallet)) {
                        ProfileRow(
                            icon = Icons.Filled.Wallet,
                            label = stringResource(R.string.profile_home_token_wallet),
                            onClick = onNavigateToTokenWallet,
                            trailingContent = uiState.tokenBalance?.let { balance ->
                                {
                                    Text(
                                        text = stringResource(R.string.token_wallet_balance_value, balance),
                                        style = WickTextStyles.BodySmall,
                                        color = WickColors.Primary,
                                    )
                                }
                            },
                        )
                        ProfileRow(
                            icon = Icons.Filled.History,
                            label = stringResource(R.string.transaction_history_title),
                            onClick = onNavigateToTransactionHistory,
                        )
                    }

                    ProfileSection(title = stringResource(R.string.profile_home_section_notifications)) {
                        ProfileRow(
                            icon = Icons.Filled.Notifications,
                            label = stringResource(R.string.profile_home_notifications),
                            onClick = onNavigateToNotifications,
                            trailingContent = if (uiState.unreadNotificationCount > 0) {
                                {
                                    Text(
                                        text = stringResource(R.string.notifications_unread_count, uiState.unreadNotificationCount),
                                        style = WickTextStyles.BodySmall,
                                        color = WickColors.Primary,
                                    )
                                }
                            } else null,
                        )
                        ProfileRow(
                            icon = Icons.Filled.NotificationsActive,
                            label = stringResource(R.string.profile_home_notification_prefs),
                            onClick = onNavigateToNotificationPreferences,
                        )
                    }

                    ProfileSection(title = stringResource(R.string.profile_home_section_other)) {
                        ProfileRow(
                            icon = Icons.AutoMirrored.Filled.Article,
                            label = stringResource(R.string.profile_home_legal_documents),
                            onClick = onNavigateToLegalDocuments,
                        )
                        ProfileRow(
                            icon = Icons.Filled.Download,
                            label = if (uiState.isExporting)
                                stringResource(R.string.profile_home_export_loading)
                            else
                                stringResource(R.string.profile_home_export_data),
                            onClick = {
                                if (!uiState.isExporting) onAction(ProfileHomeAction.ExportDataRequested)
                            },
                        )
                        ProfileRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = stringResource(R.string.profile_home_logout),
                            onClick = { onAction(ProfileHomeAction.LogoutRequested) },
                        )
                        ProfileRow(
                            icon = Icons.Filled.DeleteForever,
                            label = stringResource(R.string.profile_home_delete_account),
                            onClick = onNavigateToAccountSecurity,
                            danger = true,
                        )
                    }

                    uiState.error?.let {
                        WickInlineWarning(
                            text = stringResource(R.string.profile_home_error_unknown),
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }

        WickBottomNav(
            items = listOf(
                WickBottomNavItem(Icons.Filled.LocalFireDepartment, stringResource(R.string.profile_home_nav_discover)),
                WickBottomNavItem(Icons.Filled.Favorite, stringResource(R.string.profile_home_nav_matches)),
                WickBottomNavItem(Icons.Filled.ChatBubbleOutline, stringResource(R.string.profile_home_nav_conversations)),
                WickBottomNavItem(Icons.Filled.Person, stringResource(R.string.profile_home_nav_profile)),
            ),
            selectedIndex = 3,
            onItemSelected = { index ->
                when (index) {
                    0 -> onNavigateToDiscover()
                    1 -> onNavigateToMatches()
                    2 -> onNavigateToConversations()
                    else -> Unit
                }
            },
        )
    }

    if (uiState.showLogoutConfirmation) {
        LogoutConfirmationDialog(
            onConfirm = { onAction(ProfileHomeAction.LogoutConfirmed) },
            onDismiss = { onAction(ProfileHomeAction.LogoutCancelled) },
        )
    }
}

