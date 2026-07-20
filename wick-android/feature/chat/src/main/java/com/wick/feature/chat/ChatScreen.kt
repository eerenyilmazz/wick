package com.wick.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickAsyncImage
import com.wick.core.designsystem.component.WickInlineSuccess
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.ChatMessage
import com.wick.core.model.UiError
import com.wick.feature.chat.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChatScreenRoute(onBack: () -> Unit, onBuyTokens: () -> Unit, viewModel: ChatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(
        uiState = uiState,
        onBack = onBack,
        onDraftChanged = viewModel::onDraftChanged,
        onSend = viewModel::sendMessage,
        onRetryMessage = viewModel::retrySendMessage,
        onBlock = viewModel::blockUser,
        onReport = viewModel::reportUser,
        onExtend = viewModel::extendSession,
        onDismissExtendSheet = viewModel::dismissExtendSheet,
        onEndSession = viewModel::endSession,
        onBuyTokens = onBuyTokens,
        onClearReportSuccess = viewModel::clearReportSuccess,
    )
}

@Composable
private fun ChatScreen(
    uiState: ChatUiState,
    onBack: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onRetryMessage: (String) -> Unit,
    onBlock: () -> Unit,
    onReport: (com.wick.core.model.ReportReason) -> Unit,
    onExtend: () -> Unit,
    onDismissExtendSheet: () -> Unit,
    onEndSession: () -> Unit,
    onBuyTokens: () -> Unit,
    onClearReportSuccess: () -> Unit,
) {
    val listState = rememberLazyListState()
    val totalCount = uiState.messages.size + uiState.pendingMessages.size

    LaunchedEffect(totalCount) {
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    LaunchedEffect(uiState.reportedSuccessfully) {
        if (uiState.reportedSuccessfully) {
            kotlinx.coroutines.delay(3000)
            onClearReportSuccess()
        }
    }

    var showSafetySheet by remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(WickColors.Background)) {
            ChatTopBar(uiState = uiState, onBack = onBack, onMoreClick = { showSafetySheet = true })

            if (uiState.isReconnecting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WickColors.Urgency.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = WickColors.Urgency,
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = stringResource(R.string.chat_reconnecting),
                        style = WickTextStyles.BodySmall,
                        color = WickColors.Urgency,
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WickColors.Primary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isMine = message.senderId == uiState.currentUserId,
                            otherPhotoUrl = uiState.otherPhotoUrl,
                            otherDisplayName = uiState.otherDisplayName,
                        )
                    }
                    items(uiState.pendingMessages, key = { "pending_${it.clientId}" }) { pending ->
                        PendingMessageBubble(pending = pending, onRetry = { onRetryMessage(pending.clientId) })
                    }
                }
            }

            if (uiState.reportedSuccessfully) {
                WickInlineSuccess(
                    text = stringResource(R.string.chat_report_success),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            } else {
                uiState.error?.let { error ->
                    WickInlineWarning(
                        text = error.toDisplayMessage(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            if (uiState.sessionExpired) {
                SessionExpiredBar(
                    isExtending = uiState.isExtending,
                    manuallyEnded = uiState.sessionManuallyEnded,
                    onExtend = onExtend,
                )
            } else {
                if (uiState.isOtherTyping) {
                    Text(
                        text = stringResource(R.string.chat_typing_indicator, uiState.otherDisplayName),
                        style = WickTextStyles.BodySmall,
                        color = WickColors.TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                ChatInputBar(
                    value = uiState.draftMessage,
                    isSending = uiState.isSending,
                    onValueChange = onDraftChanged,
                    onSend = onSend,
                )
            }
        }

        if (uiState.showExtendSheet) {
            ExtendSessionSheet(
                remainingSeconds = uiState.remainingSeconds ?: 0,
                isExtending = uiState.isExtending,
                tokenCost = uiState.extensionTokenCost,
                extensionMinutes = uiState.extensionMinutes,
                extendError = uiState.extendError,
                onExtend = onExtend,
                onDismiss = onDismissExtendSheet,
                onEndSession = onEndSession,
                onBuyTokens = onBuyTokens,
            )
        }

        if (showSafetySheet) {
            com.wick.core.designsystem.component.SafetyActionSheet(
                onDismiss = { showSafetySheet = false },
                onBlock = {
                    showSafetySheet = false
                    onBlock()
                },
                onReport = { reason ->
                    showSafetySheet = false
                    onReport(reason)
                },
            )
        }
    }
}

@Composable
private fun ChatTopBar(uiState: ChatUiState, onBack: () -> Unit, onMoreClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.chat_back),
                tint = WickColors.Primary,
            )
        }
        InitialAvatar(
            name = uiState.otherDisplayName,
            photoUrl = uiState.otherPhotoUrl,
            size = 36,
        )
        Text(
            text = uiState.otherDisplayName,
            style = WickTextStyles.HeadlineMedium,
            color = WickColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        uiState.remainingSeconds?.let { seconds ->
            CountdownPill(seconds = seconds)
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
                tint = WickColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun CountdownPill(seconds: Long) {
    val clamped = seconds.coerceAtLeast(0)
    val minutes = clamped / 60
    val secs = clamped % 60
    val isUrgent = clamped in 1..300
    val color = if (isUrgent) WickColors.Urgency else WickColors.Primary
    Row(
        modifier = Modifier
            .clip(WickShapes.Chip)
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.AccessTime,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "%02d:%02d".format(minutes, secs),
            style = WickTextStyles.Label,
            color = color,
        )
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

@Composable
private fun PendingMessageBubble(pending: PendingMessage, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = pending.content,
            style = WickTextStyles.Body,
            color = WickColors.OnPrimary,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(WickShapes.Card)
                .background(if (pending.failed) WickColors.Urgency.copy(alpha = 0.6f) else WickColors.Primary.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
        if (pending.failed) {
            Text(
                text = stringResource(R.string.chat_message_failed_retry),
                style = WickTextStyles.BodySmall,
                color = WickColors.Urgency,
                modifier = Modifier
                    .clip(WickShapes.Chip)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.padding(4.dp).size(12.dp),
                color = WickColors.TextMuted,
                strokeWidth = 1.5.dp,
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    otherPhotoUrl: String? = null,
    otherDisplayName: String = "",
) {
    val timeLabel = runCatching { timeFormatter.format(Instant.parse(message.createdAt)) }.getOrDefault("")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        if (!isMine) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(0.85f),
            ) {
                if (otherPhotoUrl != null) {
                    WickAsyncImage(
                        model = otherPhotoUrl,
                        contentDescription = otherDisplayName,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(WickColors.Surface),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(WickColors.Primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = otherDisplayName.firstOrNull()?.uppercase().orEmpty(),
                            style = WickTextStyles.Label,
                            color = WickColors.OnPrimary,
                        )
                    }
                }
                Column {
                    Text(
                        text = otherDisplayName,
                        style = WickTextStyles.BodySmall,
                        color = WickColors.TextMuted,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                    Text(
                        text = message.content,
                        style = WickTextStyles.Body,
                        color = WickColors.TextPrimary,
                        modifier = Modifier
                            .clip(WickShapes.Card)
                            .background(WickColors.Surface)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        } else {
            Text(
                text = message.content,
                style = WickTextStyles.Body,
                color = WickColors.OnPrimary,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .clip(WickShapes.Card)
                    .background(WickColors.Primary)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
        if (timeLabel.isNotEmpty()) {
            Text(
                text = timeLabel,
                style = WickTextStyles.BodySmall,
                color = WickColors.TextMuted,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun ChatInputBar(value: String, isSending: Boolean, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).clip(WickShapes.Button),
            placeholder = { Text(text = stringResource(R.string.chat_input_placeholder)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = WickColors.Surface,
                unfocusedContainerColor = WickColors.Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (value.isNotBlank()) WickColors.Primary else WickColors.Surface)
                .clickable(enabled = value.isNotBlank() && !isSending, onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSend()
                }),
            contentAlignment = Alignment.Center,
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = WickColors.OnPrimary, strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.chat_send),
                    tint = if (value.isNotBlank()) WickColors.OnPrimary else WickColors.TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun SessionExpiredBar(isExtending: Boolean, manuallyEnded: Boolean, onExtend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                if (manuallyEnded) R.string.chat_session_ended else R.string.chat_session_expired,
            ),
            style = WickTextStyles.Body,
            color = WickColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        if (isExtending) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = WickColors.Primary, strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun ExtendSessionSheet(
    remainingSeconds: Long,
    isExtending: Boolean,
    tokenCost: Int,
    extensionMinutes: Int,
    extendError: UiError?,
    onExtend: () -> Unit,
    onDismiss: () -> Unit,
    onEndSession: () -> Unit,
    onBuyTokens: () -> Unit,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(WickColors.Border),
                )
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = WickColors.TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .clickable(onClick = onDismiss),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(WickShapes.Chip)
                        .background(WickColors.Urgency.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = WickColors.Urgency,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.chat_time_remaining, remainingSeconds / 60, remainingSeconds % 60),
                    style = WickTextStyles.HeadlineLarge,
                    color = WickColors.TextPrimary,
                )
            }

            Text(
                text = stringResource(R.string.chat_extend_body, extensionMinutes, tokenCost),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WickShapes.Card)
                    .background(WickColors.Primary.copy(alpha = 0.12f))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = WickColors.Primary,
                    modifier = Modifier.size(56.dp),
                )
            }

            if (extendError == UiError.InsufficientTokens) {
                WickInlineWarning(
                    text = stringResource(R.string.chat_extend_error_insufficient_tokens),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (extendError == UiError.InsufficientTokens) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WickShapes.Button)
                        .background(WickColors.Primary)
                        .clickable(onClick = onBuyTokens)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.chat_extend_buy_tokens),
                        style = WickTextStyles.Label,
                        color = WickColors.OnPrimary,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WickShapes.Button)
                        .background(WickColors.Primary)
                        .clickable(enabled = !isExtending, onClick = onExtend)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isExtending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = WickColors.OnPrimary, strokeWidth = 2.dp)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = WickColors.OnPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(R.string.chat_extend_cta, tokenCost),
                                style = WickTextStyles.Label,
                                color = WickColors.OnPrimary,
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.chat_extend_dismiss),
                style = WickTextStyles.Body,
                color = WickColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(WickShapes.Chip)
                    .clickable(onClick = onEndSession)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Unauthorized -> stringResource(R.string.chat_error_unauthorized)
    UiError.NoConnection -> stringResource(R.string.chat_error_no_connection)
    UiError.SessionNotFound -> stringResource(R.string.chat_error_session_not_found)
    UiError.InvalidLink -> stringResource(R.string.chat_error_invalid_link)
    is UiError.Message -> text
    UiError.Forbidden, UiError.Conflict, UiError.RateLimited, UiError.Unknown,
    UiError.ProductNotFound, UiError.InsufficientTokens ->
        stringResource(R.string.chat_error_unknown)
}
