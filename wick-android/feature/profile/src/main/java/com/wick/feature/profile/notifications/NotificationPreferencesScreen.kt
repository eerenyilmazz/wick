package com.wick.feature.profile.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.profile.R

@Composable
fun NotificationPreferencesRoute(
    onBack: () -> Unit,
    viewModel: NotificationPreferencesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NotificationPreferencesScreen(onBack = onBack, state = state, onToggle = { field, value -> viewModel.toggle(field, value) })
}

@Composable
private fun NotificationPreferencesScreen(
    onBack: () -> Unit,
    state: NotificationPrefsState,
    onToggle: (NotificationField, Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(WickColors.Background)) {
        WickTopBar(
            title = stringResource(R.string.notification_prefs_title),
            onBackClick = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(72.dp)
                    .clip(WickShapes.Card)
                    .background(WickColors.Surface)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = WickColors.Primary,
                    modifier = Modifier.size(36.dp),
                )
            }

            NotifPrefRow(
                title = stringResource(R.string.notification_prefs_new_matches),
                subtitle = stringResource(R.string.notification_prefs_new_matches_body),
                checked = state.newMatches,
                onCheckedChange = { onToggle(NotificationField.NewMatches, it) },
                showDivider = true,
            )
            NotifPrefRow(
                title = stringResource(R.string.notification_prefs_invites),
                subtitle = stringResource(R.string.notification_prefs_invites_body),
                checked = state.invites,
                onCheckedChange = { onToggle(NotificationField.Invites, it) },
                showDivider = true,
            )
            NotifPrefRow(
                title = stringResource(R.string.notification_prefs_messages),
                subtitle = stringResource(R.string.notification_prefs_messages_body),
                checked = state.messages,
                onCheckedChange = { onToggle(NotificationField.Messages, it) },
                showDivider = true,
            )
            NotifPrefRow(
                title = stringResource(R.string.notification_prefs_reminders),
                subtitle = stringResource(R.string.notification_prefs_reminders_body),
                checked = state.reminders,
                onCheckedChange = { onToggle(NotificationField.Reminders, it) },
                showDivider = true,
            )
            NotifPrefRow(
                title = stringResource(R.string.notification_prefs_promos),
                subtitle = stringResource(R.string.notification_prefs_promos_body),
                checked = state.promos,
                onCheckedChange = { onToggle(NotificationField.Promos, it) },
                showDivider = false,
            )

            Text(
                text = stringResource(R.string.notification_prefs_footer),
                style = WickTextStyles.BodySmall,
                color = WickColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
            )
        }
    }
}

@Composable
private fun NotifPrefRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(text = title, style = WickTextStyles.Label, color = WickColors.TextPrimary)
                Text(text = subtitle, style = WickTextStyles.BodySmall, color = WickColors.TextSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = WickColors.OnPrimary,
                    checkedTrackColor = WickColors.Primary,
                    uncheckedThumbColor = WickColors.TextMuted,
                    uncheckedTrackColor = WickColors.Border,
                    uncheckedBorderColor = WickColors.Border,
                ),
            )
        }
        if (showDivider) {
            HorizontalDivider(color = WickColors.Border.copy(alpha = 0.4f))
        }
    }
}
