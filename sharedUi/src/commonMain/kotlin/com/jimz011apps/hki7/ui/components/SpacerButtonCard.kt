package com.jimz011apps.hki7.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors

/**
 * Original HKI 7 transparent spacer button, shared by Android and web.
 *
 * It reserves exactly the production button footprint and only draws its selection outline while
 * editing. The dimensions and corner treatment are unchanged from the Android implementation.
 */
@Composable
fun SpacerButtonCard(
    isSquare: Boolean,
    buttonStyle: String,
    cornerRadius: Int,
    showOutline: Boolean,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalHKIAppColors.current
    val sizeModifier = when {
        buttonStyle == "tile" -> Modifier.height(58.dp)
        isSquare -> Modifier.aspectRatio(1f)
        else -> Modifier.height(110.dp)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(sizeModifier)
            .then(
                if (showOutline) {
                    Modifier.border(
                        1.dp,
                        appColors.onMuted.copy(alpha = 0.45f),
                        RoundedCornerShape(cornerRadius.dp),
                    )
                } else {
                    Modifier
                },
            ),
    )
}
