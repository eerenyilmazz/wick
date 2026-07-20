package com.wick.feature.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickAsyncImage
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.component.WickSectionLabel
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline

@Composable
fun IncomingInvitationsSection(
    invitations: List<IncomingInvitation>,
    respondingInvitationIds: Set<String>,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (invitations.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WickSectionLabel(icon = Icons.Filled.MailOutline, text = stringResource(R.string.matches_incoming_invitations_label))

        invitations.forEach { incoming ->
            IncomingInvitationCard(
                incoming = incoming,
                isResponding = incoming.invitation.id in respondingInvitationIds,
                onAccept = { onAccept(incoming.invitation.id) },
                onDecline = { onDecline(incoming.invitation.id) },
            )
        }
    }
}

@Composable
private fun IncomingInvitationCard(
    incoming: IncomingInvitation,
    isResponding: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Primary.copy(alpha = 0.08f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WickAsyncImage(
                model = incoming.fromUser.photos.firstOrNull()?.url,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(WickShapes.IconBox),
            )
            Text(
                text = stringResource(R.string.matches_incoming_invitation_body, incoming.fromUser.displayName),
                style = WickTextStyles.Body,
                color = WickColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WickButton(
                text = stringResource(R.string.matches_invitation_decline),
                onClick = onDecline,
                enabled = !isResponding,
                variant = WickButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
            )
            WickButton(
                text = stringResource(R.string.matches_invitation_accept),
                onClick = onAccept,
                enabled = !isResponding,
                loading = isResponding,
                variant = WickButtonVariant.Primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
