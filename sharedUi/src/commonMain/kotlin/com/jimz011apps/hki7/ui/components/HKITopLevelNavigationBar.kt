package com.jimz011apps.hki7.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.ui.Screen
import com.jimz011apps.hki7.ui.utils.MdiIcon

/**
 * The canonical HKI 7 top-level navigation bar.
 *
 * This is the original Android bar extracted into commonMain, not a web recreation. Platform hosts
 * only provide the selected-state calculation, localized labels, and navigation callback.
 */
@Composable
fun HKITopLevelNavigationBar(
    screens: List<Screen>,
    isSelected: (Screen) -> Boolean,
    onSelect: (Screen) -> Unit,
    labelFor: @Composable (Screen) -> String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 32.dp,
    scrollable: Boolean = false,
) {
    val appColors = LocalHKIAppColors.current

    HKIBottomBar(
        modifier = modifier,
        horizontalPadding = horizontalPadding,
        scrollable = scrollable,
    ) {
        screens.forEach { screen ->
            val selected = isSelected(screen)
            Column(
                modifier = Modifier
                    .then(if (scrollable) Modifier.width(68.dp) else Modifier.weight(1f))
                    .fillMaxHeight()
                    .clickable { onSelect(screen) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 32.dp)
                        .clip(itemCornerShape())
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            } else {
                                Color.Transparent
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    val iconTint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        appColors.onMuted
                    }
                    if (screen.mdiIcon != null) {
                        MdiIcon(
                            name = screen.mdiIcon,
                            tint = iconTint,
                            size = 24.dp,
                        )
                    } else {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Text(
                    text = labelFor(screen),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) appColors.onSurface else appColors.onMuted,
                    fontSize = 10.sp,
                )
            }
        }
    }
}
