package com.jimz011apps.hki7.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jimz011apps.hki7.data.HAEntity
import com.jimz011apps.hki7.resources.Res
import com.jimz011apps.hki7.resources.core_media_paused_track
import com.jimz011apps.hki7.resources.ui_card_settings_1a3b62a
import com.jimz011apps.hki7.resources.ui_remove_e963907
import com.jimz011apps.hki7.ui.localizedStateLabel
import org.jetbrains.compose.resources.stringResource

/**
 * Original HKI 7 edit-mode remove badge, shared verbatim by Android and web.
 */
@Composable
fun EditRemoveBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .offset(x = 6.dp, y = (-6).dp)
            .size(20.dp)
            .zIndex(20f)
            .background(Color(0xFF3C3C3E), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.72f), CircleShape)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = stringResource(Res.string.ui_remove_e963907),
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
    }
}

/** Original HKI 7 edit-mode settings button. */
@Composable
fun EditSettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .zIndex(2f)
            .shadow(5.dp, CircleShape)
            .background(Color.Black.copy(alpha = 0.58f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.72f), CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = stringResource(Res.string.ui_card_settings_1a3b62a),
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
    }
}

/** Original one-line status for a room's configured media player. */
@Composable
fun mediaPlayerStatus(entity: HAEntity?): String? {
    entity ?: return null
    val title = entity.mediaTitle
    return if ((entity.state == "playing" || entity.state == "paused") && !title.isNullOrBlank()) {
        val artist = entity.mediaArtist
        val track = if (!artist.isNullOrBlank()) "$title • $artist" else title
        if (entity.state == "paused") {
            stringResource(Res.string.core_media_paused_track, track)
        } else {
            track
        }
    } else {
        entity.localizedStateLabel()
    }
}

fun mediaPlayerStateIcon(entity: HAEntity?): ImageVector? {
    entity ?: return null
    return when (entity.state.lowercase()) {
        "playing" -> Icons.Default.PlayArrow
        "paused" -> Icons.Default.Pause
        else -> Icons.Default.Stop
    }
}
