package com.wick.core.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wick.core.designsystem.R
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.ReportReason

private enum class SafetyChoice { BLOCK, REPORT }

@Composable
fun SafetyActionSheet(
    onDismiss: () -> Unit,
    onBlock: () -> Unit,
    onReport: (ReportReason) -> Unit,
) {
    var choice by remember { mutableStateOf<SafetyChoice?>(null) }
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var reportExpanded by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)

    val canContinue = when (choice) {
        SafetyChoice.BLOCK -> true
        SafetyChoice.REPORT -> selectedReason != null
        null -> false
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WickShapes.Card)
                .background(WickColors.SurfaceElevated)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.safety_sheet_title),
                style = WickTextStyles.HeadlineMedium,
                color = WickColors.TextPrimary,
            )

            SafetyOptionRow(
                icon = Icons.Filled.Block,
                title = stringResource(R.string.safety_block_title),
                subtitle = stringResource(R.string.safety_block_subtitle),
                selected = choice == SafetyChoice.BLOCK,
                onClick = {
                    choice = if (choice == SafetyChoice.BLOCK) null else SafetyChoice.BLOCK.also { reportExpanded = false }
                },
            )

            SafetyOptionRow(
                icon = Icons.Filled.Flag,
                title = stringResource(R.string.safety_report_title),
                subtitle = stringResource(R.string.safety_report_subtitle),
                selected = choice == SafetyChoice.REPORT,
                expandable = true,
                expanded = reportExpanded,
                onClick = {
                    if (choice == SafetyChoice.REPORT) {
                        choice = null
                        reportExpanded = false
                        selectedReason = null
                    } else {
                        choice = SafetyChoice.REPORT
                        reportExpanded = true
                    }
                },
            )

            if (reportExpanded) {
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ReportReason.entries.forEach { reason ->
                        ReportReasonRow(
                            label = reason.toDisplayLabel(),
                            selected = selectedReason == reason,
                            onClick = { selectedReason = if (selectedReason == reason) null else reason },
                        )
                    }
                }
            }

            WickButton(
                text = stringResource(R.string.safety_continue),
                enabled = canContinue,
                onClick = {
                    when (choice) {
                        SafetyChoice.BLOCK -> onBlock()
                        SafetyChoice.REPORT -> selectedReason?.let(onReport)
                        null -> Unit
                    }
                },
            )

            Text(
                text = stringResource(R.string.safety_cancel),
                style = WickTextStyles.LinkBold,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            )
        }
    }
}

@Composable
private fun SafetyOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    expandable: Boolean = false,
    expanded: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = WickColors.TextPrimary)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = WickTextStyles.Label, color = WickColors.TextPrimary)
            Text(text = subtitle, style = WickTextStyles.BodySmall, color = WickColors.TextSecondary)
        }
        if (expandable) {
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = WickColors.TextSecondary,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (selected) WickColors.Primary else Color.Transparent)
                    .border(width = 1.5.dp, color = if (selected) WickColors.Primary else WickColors.TextMuted, shape = CircleShape),
            )
        }
    }
}

@Composable
private fun ReportReasonRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Chip)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (selected) WickColors.Primary else Color.Transparent)
                .border(width = 1.5.dp, color = if (selected) WickColors.Primary else WickColors.TextMuted, shape = CircleShape),
        )
        Text(text = label, style = WickTextStyles.Body, color = WickColors.TextPrimary)
    }
}

@Composable
private fun ReportReason.toDisplayLabel(): String = when (this) {
    ReportReason.INAPPROPRIATE_CONTENT -> stringResource(R.string.safety_reason_inappropriate_content)
    ReportReason.FAKE_PROFILE -> stringResource(R.string.safety_reason_fake_profile)
    ReportReason.HARASSMENT -> stringResource(R.string.safety_reason_harassment)
    ReportReason.SPAM -> stringResource(R.string.safety_reason_spam)
    ReportReason.OTHER -> stringResource(R.string.safety_reason_other)
}
