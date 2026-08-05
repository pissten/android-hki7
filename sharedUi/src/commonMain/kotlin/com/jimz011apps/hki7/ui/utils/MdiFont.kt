package com.jimz011apps.hki7.ui.utils

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.sharedui.icons.Hki7Icon
import com.jimz011apps.hki7.sharedui.icons.Hki7IconPack

typealias IconPack = Hki7IconPack

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

object IconPreferences {
    var defaultPack: IconPack = IconPack.DEFAULT
}

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
