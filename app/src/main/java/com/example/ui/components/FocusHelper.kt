package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonBlue

@Composable
fun Modifier.tvFocusGlow(
    shapeRadius: Dp = 12.dp,
    focusedScale: Float = 1.05f,
    glowColor: Color = NeonBlue
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) focusedScale else 1.0f, label = "focusScale")

    return this
        .onFocusChanged { isFocused = it.isFocused }
        .scale(scale)
        .then(
            if (isFocused) {
                Modifier
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(shapeRadius), spotColor = glowColor, ambientColor = glowColor)
                    .border(width = 2.dp, color = glowColor, shape = RoundedCornerShape(shapeRadius))
            } else {
                Modifier
            }
        )
        .focusable()
}
