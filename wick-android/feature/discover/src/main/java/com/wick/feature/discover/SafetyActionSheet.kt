package com.wick.feature.discover

import com.wick.core.model.ReportReason
import androidx.compose.runtime.Composable

@Composable
fun SafetyActionSheet(
    onDismiss: () -> Unit,
    onBlock: () -> Unit,
    onReport: (ReportReason) -> Unit,
) = com.wick.core.designsystem.component.SafetyActionSheet(
    onDismiss = onDismiss,
    onBlock = onBlock,
    onReport = onReport,
)
