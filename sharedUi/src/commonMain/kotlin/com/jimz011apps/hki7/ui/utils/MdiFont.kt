package com.jimz011apps.hki7.ui.utils

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.sharedui.icons.Hki7Icon
import com.jimz011apps.hki7.sharedui.icons.Hki7IconPack

/** Existing HKI 7 API name backed by the shared Compose Multiplatform icon pack. */
typealias IconPack = Hki7IconPack

/** Android's synchronous picker index still reads its bundled asset copies during migration. */
val IconPack.codepointsAsset: String
    get() = when (this) {
        IconPack.MDI -> "mdi_codepoints.txt"
        IconPack.SIMPLE -> "simple_codepoints.txt"
        IconPack.TABLER -> "tabler_codepoints.txt"
        IconPack.PHOSPHOR -> "phosphor_codepoints.txt"
    }

val IconPack.keywordsAsset: String
    get() = when (this) {
        IconPack.MDI -> "mdi_keywords.txt"
        IconPack.SIMPLE -> "simple_keywords.txt"
        IconPack.TABLER -> "tabler_keywords.txt"
        IconPack.PHOSPHOR -> "phosphor_keywords.txt"
    }

/** Mirrored preference used by the existing icon picker when creating a new icon. */
object IconPreferences {
    @Volatile
    var defaultPack: IconPack = IconPack.DEFAULT
}

/**
 * Renders the same persisted qualified icon slug on Android and web from the same bundled fonts and
 * lookup tables. The public signature is unchanged, so existing HKI 7 screens can move to shared UI
 * without icon-specific rewrites.
 */
@Composable
fun MdiIcon(
    name: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    contentDescription: String? = null,
) {
    Hki7Icon(
        name = name,
        modifier = modifier,
        tint = tint,
        size = size,
        contentDescription = contentDescription,
    )
}
