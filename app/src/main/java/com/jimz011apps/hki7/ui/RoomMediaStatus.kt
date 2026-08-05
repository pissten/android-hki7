package com.jimz011apps.hki7.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.jimz011apps.hki7.R
import com.jimz011apps.hki7.ui.components.mediaPlayerStatus

@Composable
internal fun RoomMediaSummary.localizedText(): String? = when {
    text == null -> null
    activeCount == 0 -> stringResource(R.string.core_no_media_playing)
    activeCount > 1 -> pluralStringResource(
        R.plurals.core_media_players_playing,
        activeCount,
        activeCount
    )
    else -> mediaPlayerStatus(representative)
}
