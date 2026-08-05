package com.jimz011apps.hki7.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.data.HKISubtitleWidget
import com.jimz011apps.hki7.data.isWidgetVisibleNow
import com.jimz011apps.hki7.resources.Res
import com.jimz011apps.hki7.resources.ui_delete_f6fdbe4
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.ui.components.EditSettingsButton
import com.jimz011apps.hki7.ui.utils.MdiIcon
import org.jetbrains.compose.resources.stringResource

/**
 * Original HKI 7 dashboard subtitle renderer, shared unchanged by Android and web.
 *
 * Its spacing, typography, optional MDI icon and edit controls mirror the production Android
 * implementation. Platform hosts only provide mutation callbacks while editing.
 */
@Composable
fun SubtitleWidget(
    widget: HKISubtitleWidget,
    isEditMode: Boolean,
    onDelete: () -> Unit,
    onSettings: () -> Unit,
) {
    val appColors = LocalHKIAppColors.current
    if (!isWidgetVisibleNow(widget) && !isEditMode) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
    ) {
        if (!widget.icon.isNullOrBlank()) {
            MdiIcon(widget.icon, tint = appColors.onMuted, size = 22.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = widget.text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.onSurface,
        )
        if (isEditMode) {
            Spacer(Modifier.weight(1f))
            EditSettingsButton(onClick = onSettings)
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.ui_delete_f6fdbe4),
                    tint = appColors.onMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
