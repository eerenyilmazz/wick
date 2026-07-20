package com.wick.feature.profile.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickAsyncImage
import com.wick.core.designsystem.component.WickCard
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.Photo
import com.wick.feature.profile.R

@Composable
fun PhotoGridSection(
    photos: List<Photo>,
    canAddMorePhotos: Boolean,
    isUploadingPhoto: Boolean,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    onSetAsCover: ((String) -> Unit)? = null,
    onMovePhoto: ((from: Int, to: Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val photoCells: List<Photo?> = photos + if (canAddMorePhotos) listOf(null) else emptyList()
    val columns = when (photoCells.size) {
        1 -> 1
        2 -> 2
        4 -> 2
        else -> 3
    }

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val tileOffsets = remember { mutableMapOf<Int, Offset>() }
    val tileSizes = remember { mutableMapOf<Int, Float>() }

    fun findTargetIndex(globalDragPos: Offset): Int {
        var best = -1
        var bestDist = Float.MAX_VALUE
        for ((idx, pos) in tileOffsets) {
            val tileSize = tileSizes[idx] ?: continue
            val center = Offset(pos.x + tileSize / 2f, pos.y + tileSize / 2f)
            val dist = (globalDragPos - center).getDistance()
            if (dist < bestDist) {
                bestDist = dist
                best = idx
            }
        }
        return best
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        photoCells.chunked(columns).forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEachIndexed { colIndex, photo ->
                    val globalIndex = rowIndex * columns + colIndex
                    if (photo != null) {
                        val isDragged = draggedIndex == globalIndex
                        val isTarget = targetIndex == globalIndex && draggedIndex != globalIndex

                        PhotoTile(
                            photo = photo,
                            isCover = globalIndex == 0,
                            onRemove = { onRemovePhoto(photo.id) },
                            onSetAsCover = if (globalIndex != 0 && onSetAsCover != null) {
                                { onSetAsCover(photo.id) }
                            } else null,
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coords ->
                                    tileOffsets[globalIndex] = coords.positionInParent()
                                    tileSizes[globalIndex] = coords.size.width.toFloat()
                                }
                                .scale(if (isDragged) 1.08f else 1f)
                                .then(
                                    if (isTarget) Modifier.drawBehind {
                                        drawRoundRect(
                                            color = WickColors.Primary,
                                            cornerRadius = CornerRadius(18.dp.toPx()),
                                            style = Stroke(width = 2.dp.toPx()),
                                        )
                                    } else Modifier,
                                )
                                .then(
                                    if (onMovePhoto != null) {
                                        Modifier.pointerInput(photos.size) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggedIndex = globalIndex
                                                    targetIndex = globalIndex
                                                },
                                                onDrag = { change, _ ->
                                                    val pos = change.position + (tileOffsets[globalIndex] ?: Offset.Zero)
                                                    dragOffset = pos
                                                    targetIndex = findTargetIndex(pos)
                                                },
                                                onDragEnd = {
                                                    if (draggedIndex != -1 && targetIndex != -1 && draggedIndex != targetIndex) {
                                                        onMovePhoto(draggedIndex, targetIndex)
                                                    }
                                                    draggedIndex = -1
                                                    targetIndex = -1
                                                    dragOffset = Offset.Zero
                                                },
                                                onDragCancel = {
                                                    draggedIndex = -1
                                                    targetIndex = -1
                                                    dragOffset = Offset.Zero
                                                },
                                            )
                                        }
                                    } else Modifier,
                                ),
                        )
                    } else {
                        AddPhotoTile(
                            isUploading = isUploadingPhoto,
                            onClick = onAddPhoto,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                repeat(columns - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PhotoTile(
    photo: Photo,
    isCover: Boolean,
    onRemove: () -> Unit,
    onSetAsCover: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    WickCard(modifier = modifier.aspectRatio(1f)) {
        WickAsyncImage(
            model = photo.url,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
        )

        if (isCover) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(WickShapes.Chip)
                    .background(WickColors.Primary.copy(alpha = 0.85f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = WickColors.OnPrimary,
                        modifier = Modifier.size(10.dp),
                    )
                    Text(
                        text = stringResource(R.string.photo_grid_cover_label),
                        style = WickTextStyles.BodySmall,
                        color = WickColors.OnPrimary,
                    )
                }
            }
        } else if (onSetAsCover != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(WickShapes.Chip)
                    .background(WickColors.Background.copy(alpha = 0.7f))
                    .clickable(onClick = onSetAsCover)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            ) {
                Text(
                    text = stringResource(R.string.photo_grid_set_cover),
                    style = WickTextStyles.BodySmall,
                    color = WickColors.TextPrimary,
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(WickShapes.IconBox)
                    .background(WickColors.UrgencyBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.profile_bio_remove_photo),
                    tint = WickColors.Urgency,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AddPhotoTile(isUploading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .dashedBorder(color = WickColors.TextMuted, cornerRadius = 18.dp)
            .clickable(onClick = onClick, enabled = !isUploading),
        contentAlignment = Alignment.Center,
    ) {
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = WickColors.Primary,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.profile_bio_add_photo),
                tint = WickColors.Primary,
            )
        }
    }
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.5.dp) = drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
    )
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = stroke,
    )
}
