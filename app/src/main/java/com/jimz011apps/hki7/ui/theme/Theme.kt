@file:Suppress("SpellCheckingInspection")

package com.jimz011apps.hki7.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.jimz011apps.hki7.R
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors as SharedLocalHKIAppColors
import com.jimz011apps.hki7.sharedui.hki7AppColors
import com.jimz011apps.hki7.sharedui.hki7ThemedColorScheme

typealias HKIAppColors = com.jimz011apps.hki7.sharedui.HKIAppColors

/** Existing Android imports keep working while the actual design model lives in sharedUi. */
val LocalHKIAppColors = SharedLocalHKIAppColors

/** Readable bundled families. Bundling avoids OEM aliases silently resolving to the same font. */
private val NunitoFontFamily = FontFamily(Font(R.font.nunito))
private val ComfortaaFontFamily = FontFamily(Font(R.font.comfortaa))
private val SpaceGroteskFontFamily = FontFamily(Font(R.font.space_grotesk))
private val BreeSerifFontFamily = FontFamily(Font(R.font.bree_serif))
private val PatrickHandFontFamily = FontFamily(Font(R.font.patrick_hand))
private val AtkinsonHyperlegibleFontFamily = FontFamily(Font(R.font.atkinson_hyperlegible_next))

val AppFontFamilyOptions = listOf(
    "default" to "Default",
    "sans" to "Sans Serif",
    "serif" to "Serif",
    "monospace" to "Monospace",
    "cursive" to "Cursive",
    "nunito" to "Nunito",
    "comfortaa" to "Comfortaa",
    "space_grotesk" to "Space Grotesk",
    "bree_serif" to "Bree Serif",
    "patrick_hand" to "Patrick Hand",
    "atkinson" to "Atkinson Hyperlegible"
)

fun appFontFamily(key: String): FontFamily? = when (key) {
    "sans" -> FontFamily.SansSerif
    "serif" -> FontFamily.Serif
    "monospace" -> FontFamily.Monospace
    "cursive" -> FontFamily.Cursive
    "nunito" -> NunitoFontFamily
    "comfortaa" -> ComfortaaFontFamily
    "space_grotesk" -> SpaceGroteskFontFamily
    "bree_serif" -> BreeSerifFontFamily
    "patrick_hand" -> PatrickHandFontFamily
    "atkinson" -> AtkinsonHyperlegibleFontFamily
    else -> null
}

@Composable
fun HKI7Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: String = "system",
    themeMode: String = "system",
    systemLightThemeColor: String = "auto",
    systemDarkThemeColor: String = "auto",
    fontScale: Float = 1f,
    fontWeightAdjust: Int = 0,
    fontFamily: String = "default",
    itemCornerRadius: Int = 20,
    // Dynamic color is available on Android 12+ and remains an Android-only enhancement.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val effectiveDarkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> darkTheme
    }
    val systemOverride = if (effectiveDarkTheme) systemDarkThemeColor else systemLightThemeColor
    val colorScheme = when {
        themeColor != "system" -> hki7ThemedColorScheme(themeColor, effectiveDarkTheme)
        systemOverride != "auto" -> hki7ThemedColorScheme(systemOverride, effectiveDarkTheme)
        dynamicColor -> {
            val context = LocalContext.current
            if (effectiveDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> hki7ThemedColorScheme("system", effectiveDarkTheme)
    }
    val appColors = hki7AppColors(colorScheme, effectiveDarkTheme)
    val useDarkSystemBarIcons = appColors.background.luminance() > 0.5f
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Android owns the system bars; HKI 7 still owns the colors drawn beneath them.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkSystemBarIcons
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = useDarkSystemBarIcons
        }
    }

    val typography = remember(fontScale, fontWeightAdjust, fontFamily) {
        adjustedTypography(Typography(), fontScale, fontWeightAdjust, fontFamily)
    }
    val shapes = remember(itemCornerRadius) {
        val itemShape = RoundedCornerShape(itemCornerRadius.dp)
        Shapes(
            extraSmall = itemShape,
            small = itemShape,
            medium = itemShape,
            large = itemShape,
            extraLarge = itemShape
        )
    }
    CompositionLocalProvider(LocalHKIAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

/** Applies the user's font preferences (size multiplier, weight offset, family) to every style. */
private fun adjustedTypography(base: Typography, scale: Float, weightAdjust: Int, familyKey: String): Typography {
    val family = appFontFamily(familyKey)
    if (scale == 1f && weightAdjust == 0 && family == null) return base

    fun TextStyle.adjust(): TextStyle {
        val newWeight = if (weightAdjust == 0) fontWeight else {
            FontWeight(((fontWeight ?: FontWeight.Normal).weight + weightAdjust).coerceIn(100, 900))
        }
        return copy(
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize * scale else fontSize,
            lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight * scale else lineHeight,
            fontWeight = newWeight,
            fontFamily = family ?: fontFamily
        )
    }
    return Typography(
        displayLarge = base.displayLarge.adjust(),
        displayMedium = base.displayMedium.adjust(),
        displaySmall = base.displaySmall.adjust(),
        headlineLarge = base.headlineLarge.adjust(),
        headlineMedium = base.headlineMedium.adjust(),
        headlineSmall = base.headlineSmall.adjust(),
        titleLarge = base.titleLarge.adjust(),
        titleMedium = base.titleMedium.adjust(),
        titleSmall = base.titleSmall.adjust(),
        bodyLarge = base.bodyLarge.adjust(),
        bodyMedium = base.bodyMedium.adjust(),
        bodySmall = base.bodySmall.adjust(),
        labelLarge = base.labelLarge.adjust(),
        labelMedium = base.labelMedium.adjust(),
        labelSmall = base.labelSmall.adjust()
    )
}
