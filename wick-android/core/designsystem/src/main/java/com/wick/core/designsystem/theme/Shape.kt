package com.wick.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object WickShapes {
    val Button = RoundedCornerShape(23.dp)
    val Card = RoundedCornerShape(18.dp)
    val Chip = RoundedCornerShape(12.dp)
    val IconBox = RoundedCornerShape(18.dp)
    val Sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}

val WickMaterialShapes = Shapes(
    extraSmall = WickShapes.Chip,
    small = WickShapes.Chip,
    medium = WickShapes.Card,
    large = WickShapes.Button,
    extraLarge = WickShapes.Sheet,
)
