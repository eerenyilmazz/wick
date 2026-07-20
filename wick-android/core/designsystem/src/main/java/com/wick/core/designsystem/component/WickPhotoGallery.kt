package com.wick.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes

@Composable
fun WickPhotoGallery(
    photoUrls: List<String>,
    height: Dp,
    modifier: Modifier = Modifier,
    overlayContent: (@Composable () -> Unit)? = null,
) {
    val pagerState = rememberPagerState(pageCount = { photoUrls.size.coerceAtLeast(1) })

    Box(modifier = modifier.fillMaxWidth().height(height)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            WickAsyncImage(
                model = photoUrls.getOrNull(page),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (photoUrls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(photoUrls.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(width = if (index == pagerState.currentPage) 24.dp else 6.dp, height = 6.dp)
                            .clip(WickShapes.Chip)
                            .background(
                                if (index == pagerState.currentPage) {
                                    WickColors.Primary
                                } else {
                                    WickColors.TextPrimary.copy(alpha = 0.3f)
                                },
                            ),
                    )
                }
            }
        }

        if (overlayContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(WickColors.Background.copy(alpha = 0.45f)),
            ) {
                overlayContent()
            }
        }
    }
}
