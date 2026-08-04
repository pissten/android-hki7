package com.jimz011apps.hki7.ui.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Subtle depth gradient built purely from a surface's own color (two shades of it, no accent). */
fun hki7SurfaceGradient(base: Color): Brush = Brush.verticalGradient(
    listOf(
        blendSurfaceColor(base, Color.White, 0.06f),
        base,
        blendSurfaceColor(base, Color.Black, 0.10f)
    )
)

private fun blendSurfaceColor(start: Color, end: Color, fraction: Float): Color = Color(
    red = start.red + (end.red - start.red) * fraction,
    green = start.green + (end.green - start.green) * fraction,
    blue = start.blue + (end.blue - start.blue) * fraction,
    alpha = 1f
)
