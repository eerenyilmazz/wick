package com.wick.feature.profile.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.min
import com.wick.core.designsystem.component.WickAsyncImage
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.Photo

@Composable
fun PhotoFan(photos: List<Photo>, fallbackInitial: String, modifier: Modifier = Modifier) {
    if (photos.isEmpty()) {
        Box(
            modifier = modifier
                .size(110.dp)
                .clip(WickShapes.Card)
                .background(WickColors.Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = fallbackInitial, style = WickTextStyles.HeadlineLarge, color = WickColors.OnPrimary)
        }
        return
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val scale = min(1f, maxWidth.value / 220f)
        val containerW = (220 * scale).dp
        val containerH = (180 * scale).dp
        val centerPhotoSize = DpSize((120 * scale).dp, (160 * scale).dp)
        val sidePhotoSize = DpSize((100 * scale).dp, (140 * scale).dp)
        val sideOffset = (55 * scale).dp

        Box(modifier = Modifier.size(containerW, containerH), contentAlignment = Alignment.Center) {
            photos.getOrNull(2)?.let { photo ->
                FannedPhoto(photo = photo, rotationDegrees = -14f, offsetX = -sideOffset, size = sidePhotoSize)
            }
            photos.getOrNull(1)?.let { photo ->
                FannedPhoto(photo = photo, rotationDegrees = 14f, offsetX = sideOffset, size = sidePhotoSize)
            }
            FannedPhoto(photo = photos[0], rotationDegrees = 0f, offsetX = 0.dp, size = centerPhotoSize)
        }
    }
}

@Composable
private fun FannedPhoto(
    photo: Photo,
    rotationDegrees: Float,
    offsetX: Dp,
    size: DpSize = DpSize(100.dp, 140.dp),
) {
    WickAsyncImage(
        model = photo.url,
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .offset(x = offsetX)
            .graphicsLayer { rotationZ = rotationDegrees }
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .border(width = 1.5.dp, color = WickColors.Primary.copy(alpha = 0.6f), shape = WickShapes.Card),
    )
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title.uppercase(),
            style = WickTextStyles.BodySmall,
            color = WickColors.TextMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )
        content()
    }
}

@Composable
fun ProfileRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    enabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val backgroundColor = if (danger) WickColors.UrgencyBg else WickColors.Surface
    val iconTint = if (!enabled) WickColors.TextMuted else if (danger) WickColors.Urgency else WickColors.Primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            style = WickTextStyles.Body,
            color = if (enabled) WickColors.TextPrimary else WickColors.TextMuted,
            modifier = Modifier.weight(1f),
        )
        trailingContent?.invoke()
    }
}
