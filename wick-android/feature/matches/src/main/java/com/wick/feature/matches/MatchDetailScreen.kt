package com.wick.feature.matches

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickPhotoGallery
import com.wick.core.designsystem.component.WickSectionLabel
import com.wick.core.designsystem.component.WickVerifiedBadge
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.MatchSummary
import com.wick.core.model.UiError
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MatchDetailScreen(
    match: MatchSummary,
    invitationState: InvitationSendState,
    isUnmatching: Boolean,
    isCancelling: Boolean = false,
    retryTokenCost: Int = 1,
    retryLimitReached: Boolean = false,
    onBack: () -> Unit,
    onInvite: () -> Unit,
    onRetryInvite: () -> Unit = {},
    onCancelInvitation: () -> Unit = {},
    onOpenChat: () -> Unit,
    onUnmatch: () -> Unit,
) {
    BackHandler(onBack = onBack)

    var menuExpanded by remember { mutableStateOf(false) }
    var showUnmatchConfirm by remember { mutableStateOf(false) }

    val user = match.otherUser
    val age = user.dateOfBirth?.let { rawDate ->
        runCatching { Period.between(LocalDate.parse(rawDate), LocalDate.now()).years }.getOrNull()
    }
    val matchedOn = runCatching {
        DateTimeFormatter.ofPattern("d MMMM yyyy")
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(match.createdAt))
    }.getOrDefault("")
    val location = listOfNotNull(user.province, user.district).joinToString(", ")
    val photoHeight = (LocalConfiguration.current.screenHeightDp * 0.5f).dp

    Box(modifier = Modifier.fillMaxSize().background(WickColors.Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            WickPhotoGallery(
                photoUrls = user.photos.map { it.url },
                height = photoHeight,
                overlayContent = {
                    MatchDetailHeaderOverlay(
                        displayName = user.displayName,
                        age = age,
                        verified = user.verified,
                        location = location,
                    )
                },
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (matchedOn.isNotEmpty()) {
                    MatchedOnBadge(matchedOn = matchedOn)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WickSectionLabel(
                        icon = Icons.AutoMirrored.Filled.Article,
                        text = stringResource(R.string.match_detail_about_label),
                    )
                    Text(
                        text = user.bio?.takeIf { it.isNotBlank() } ?: stringResource(R.string.match_detail_about_empty),
                        style = WickTextStyles.Body,
                        color = WickColors.TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(WickShapes.Card)
                            .background(WickColors.Surface)
                            .padding(16.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WickSectionLabel(
                        icon = Icons.Filled.Favorite,
                        text = stringResource(R.string.match_detail_interests_label),
                    )
                    if (user.interests.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(user.interests, key = { it }) { interest ->
                                Text(
                                    text = interest,
                                    style = WickTextStyles.Label,
                                    color = WickColors.Primary,
                                    modifier = Modifier
                                        .clip(WickShapes.Chip)
                                        .background(WickColors.Primary.copy(alpha = 0.12f))
                                        .border(
                                            width = 1.dp,
                                            color = WickColors.Primary.copy(alpha = 0.3f),
                                            shape = WickShapes.Chip,
                                        )
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                )
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.match_detail_interests_empty),
                            style = WickTextStyles.Body,
                            color = WickColors.TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(WickShapes.Card)
                                .background(WickColors.Surface)
                                .padding(16.dp),
                        )
                    }
                }

                InviteSection(
                    invitationState = invitationState,
                    isCancelling = isCancelling,
                    retryTokenCost = retryTokenCost,
                    retryLimitReached = retryLimitReached,
                    onInvite = onInvite,
                    onRetryInvite = onRetryInvite,
                    onCancelInvitation = onCancelInvitation,
                    onOpenChat = onOpenChat,
                )
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 12.dp, top = 12.dp)
                .size(40.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Background.copy(alpha = 0.6f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.match_detail_back),
                tint = WickColors.Primary,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 12.dp, top = 12.dp),
        ) {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(WickShapes.IconBox)
                    .background(WickColors.Background.copy(alpha = 0.6f)),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.match_detail_more_options),
                    tint = WickColors.Primary,
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.match_detail_unmatch_action)) },
                    onClick = {
                        menuExpanded = false
                        showUnmatchConfirm = true
                    },
                )
            }
        }
    }

    if (showUnmatchConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isUnmatching) showUnmatchConfirm = false },
            title = { Text(stringResource(R.string.match_detail_unmatch_confirm_title)) },
            text = { Text(stringResource(R.string.match_detail_unmatch_confirm_body, user.displayName)) },
            confirmButton = {
                TextButton(onClick = onUnmatch, enabled = !isUnmatching) {
                    Text(stringResource(R.string.match_detail_unmatch_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnmatchConfirm = false }, enabled = !isUnmatching) {
                    Text(stringResource(R.string.match_detail_unmatch_cancel_action))
                }
            },
        )
    }
}

@Composable
private fun MatchDetailHeaderOverlay(displayName: String, age: Int?, verified: Boolean, location: String) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (age != null) stringResource(R.string.match_detail_age_name, displayName, age) else displayName,
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
            )
            if (verified) {
                WickVerifiedBadge(contentDescription = stringResource(R.string.match_detail_verified))
            }
        }

        if (location.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = WickColors.Primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(text = location, style = WickTextStyles.Body, color = WickColors.TextPrimary)
            }
        }
    }
}

@Composable
private fun MatchedOnBadge(matchedOn: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Primary.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = Icons.Filled.LocalFireDepartment, contentDescription = null, tint = WickColors.Primary)
        Text(
            text = stringResource(R.string.match_detail_matched_on, matchedOn),
            style = WickTextStyles.Label,
            color = WickColors.TextPrimary,
        )
    }
}

@Composable
private fun InviteSection(
    invitationState: InvitationSendState,
    isCancelling: Boolean,
    retryTokenCost: Int,
    retryLimitReached: Boolean,
    onInvite: () -> Unit,
    onRetryInvite: () -> Unit,
    onCancelInvitation: () -> Unit,
    onOpenChat: () -> Unit,
) {
    when (invitationState) {
        is InvitationSendState.Sent -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InviteStatusBanner(
                icon = Icons.Filled.MailOutline,
                text = stringResource(R.string.match_detail_invite_sent),
            )
            WickButton(
                text = stringResource(R.string.match_detail_invite_cancel),
                onClick = onCancelInvitation,
                enabled = !isCancelling,
                loading = isCancelling,
                variant = WickButtonVariant.Secondary,
            )
        }

        is InvitationSendState.IncomingPending -> InviteStatusBanner(
            icon = Icons.Filled.MailOutline,
            text = stringResource(R.string.match_detail_invite_incoming),
        )

        is InvitationSendState.Accepted -> WickButton(
            text = stringResource(R.string.match_detail_open_chat),
            onClick = onOpenChat,
            variant = WickButtonVariant.Primary,
        )

        is InvitationSendState.Declined -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InviteStatusBanner(
                icon = Icons.Filled.MailOutline,
                text = stringResource(R.string.match_detail_invite_declined),
            )
            if (retryLimitReached) {
                WickInlineWarning(text = stringResource(R.string.match_detail_invite_retry_limit_reached))
            } else {
                WickButton(
                    text = stringResource(R.string.match_detail_invite_retry_action, retryTokenCost),
                    onClick = onRetryInvite,
                    variant = WickButtonVariant.Secondary,
                )
            }
        }

        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WickButton(
                text = stringResource(R.string.match_detail_invite_action),
                onClick = onInvite,
                enabled = invitationState !is InvitationSendState.Sending,
                loading = invitationState is InvitationSendState.Sending,
                variant = WickButtonVariant.Primary,
            )
            if (invitationState is InvitationSendState.Failed) {
                WickInlineWarning(text = invitationState.error.toInviteErrorMessage())
            }
        }
    }
}

@Composable
private fun InviteStatusBanner(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Primary.copy(alpha = 0.12f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = WickColors.Primary)
        Text(text = text, style = WickTextStyles.Body, color = WickColors.TextPrimary)
    }
}

@Composable
private fun UiError.toInviteErrorMessage(): String = when (this) {
    UiError.Unauthorized -> stringResource(R.string.match_detail_error_unauthorized)
    UiError.NoConnection -> stringResource(R.string.match_detail_error_no_connection)
    UiError.Conflict -> stringResource(R.string.match_detail_error_conflict)
    UiError.RateLimited -> stringResource(R.string.match_detail_error_rate_limited)
    is UiError.Message -> text
    UiError.Forbidden, UiError.Unknown,
    UiError.InvalidLink, UiError.ProductNotFound, UiError.SessionNotFound, UiError.InsufficientTokens -> stringResource(R.string.match_detail_error_unknown)
}
