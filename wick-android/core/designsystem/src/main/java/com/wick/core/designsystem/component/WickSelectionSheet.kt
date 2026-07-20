package com.wick.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun WickSelectionSheet(
    title: String,
    options: List<String>,
    searchPlaceholder: String,
    emptyMessage: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    selectedValue: String? = null,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, options) {
        if (query.isBlank()) options else options.filter { it.contains(query, ignoreCase = true) }
    }
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * 0.70).dp

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
                .shadow(elevation = 24.dp, shape = WickShapes.Card)
                .clip(WickShapes.Card)
                .background(WickColors.SurfaceElevated)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(WickShapes.Chip)
                        .background(WickColors.TextMuted.copy(alpha = 0.4f)),
                )
            }

            Text(text = title, style = WickTextStyles.HeadlineMedium, color = WickColors.TextPrimary)

            WickTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = searchPlaceholder,
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = WickColors.TextMuted)
                },
            )

            if (filtered.isEmpty()) {
                Text(text = emptyMessage, style = WickTextStyles.Body, color = WickColors.TextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filtered, key = { it }) { option ->
                        SelectionRow(
                            label = option,
                            selected = option == selectedValue,
                            onClick = { onOptionSelected(option) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(if (selected) WickColors.Primary.copy(alpha = 0.12f) else WickColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = WickColors.Primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = label,
            style = WickTextStyles.Body,
            color = WickColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = WickColors.Primary)
        }
    }
}
