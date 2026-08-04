package com.jimz011apps.hki7.sharedui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * Platform-neutral HKI 7 color roles. Android and browser code should consume this same model so
 * cards, dialogs, backgrounds, and muted text do not drift into separate design implementations.
 */
data class HKIAppColors(
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val onSurface: Color,
    val onMuted: Color,
    val subtleSurface: Color,
    val headerFallbackStart: Color,
    val headerFallbackEnd: Color,
    val accent: Color,
)

val LocalHKIAppColors = staticCompositionLocalOf {
    HKIAppColors(
        background = Color.Black,
        surface = Color(0xFF1C1C1E),
        elevated = Color(0xFF2C2C2E),
        onSurface = Color.White,
        onMuted = Color.Gray,
        subtleSurface = Color.White.copy(alpha = 0.08f),
        headerFallbackStart = Color(0xFF5D1029),
        headerFallbackEnd = Color.Black,
        accent = Color(0xFFD0BCFF),
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
    background = Color.Black,
    surface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFF2C2C2E),
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6650A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFF8F5F7),
    surface = Color.White,
    surfaceVariant = Color(0xFFEDE7EA),
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun HKI7SharedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: String = "system",
    themeMode: String = "system",
    systemLightThemeColor: String = "auto",
    systemDarkThemeColor: String = "auto",
    itemCornerRadius: Int = 20,
    typography: Typography = Typography(),
    content: @Composable () -> Unit,
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
        effectiveDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val appColors = hki7AppColors(colorScheme, effectiveDarkTheme)
    val shapes = remember(itemCornerRadius) {
        val itemShape = RoundedCornerShape(itemCornerRadius.dp)
        Shapes(
            extraSmall = itemShape,
            small = itemShape,
            medium = itemShape,
            large = itemShape,
            extraLarge = itemShape,
        )
    }

    CompositionLocalProvider(LocalHKIAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}

fun hki7ThemedColorScheme(theme: String, darkTheme: Boolean): ColorScheme = when {
    theme.startsWith("custom:", ignoreCase = true) -> {
        val custom = parseThemeColor(theme.substringAfter("custom:")) ?: Color(0xFF9B5353)
        appScheme(custom, darkTheme)
    }
    theme == "rose" -> appScheme(Color(0xFF9B5353), darkTheme)
    theme == "green" -> appScheme(Color(0xFF4CAF50), darkTheme)
    theme == "blue" -> appScheme(Color(0xFF1E90FF), darkTheme)
    theme == "amber" -> appScheme(Color(0xFFFFB300), darkTheme)
    else -> if (darkTheme) DarkColorScheme else LightColorScheme
}

fun hki7AppColors(colorScheme: ColorScheme, darkTheme: Boolean): HKIAppColors =
    if (darkTheme) {
        HKIAppColors(
            background = Color.Black,
            surface = Color(0xFF1C1C1E),
            elevated = Color(0xFF2C2C2E),
            onSurface = Color.White,
            onMuted = Color(0xFF9A9A9A),
            subtleSurface = Color.White.copy(alpha = 0.08f),
            headerFallbackStart = colorScheme.primary.copy(alpha = 0.45f),
            headerFallbackEnd = Color.Black,
            accent = colorScheme.primary,
        )
    } else {
        HKIAppColors(
            background = Color(0xFFF8F5F7),
            surface = Color.White,
            elevated = Color(0xFFEDE7EA),
            onSurface = Color(0xFF1C1B1F),
            onMuted = Color(0xFF6F676B),
            subtleSurface = colorScheme.primary.copy(alpha = 0.08f),
            headerFallbackStart = colorScheme.primaryContainer,
            headerFallbackEnd = Color(0xFFF8F5F7),
            accent = colorScheme.primary,
        )
    }

private fun parseThemeColor(value: String): Color? {
    val raw = value.trim().removePrefix("#")
    val argb = when (raw.length) {
        6 -> "FF$raw"
        8 -> raw
        else -> return null
    }
    return argb.toULongOrNull(16)?.let(::Color)
}

private fun appScheme(primary: Color, darkTheme: Boolean): ColorScheme = if (darkTheme) {
    val onPrimary = readableOn(primary)
    darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = primary.copy(alpha = 0.85f),
        tertiary = primary.copy(alpha = 0.7f),
        primaryContainer = primary.copy(alpha = 0.35f),
        onPrimaryContainer = onPrimary,
        surface = Color(0xFF1C1C1E),
        surfaceVariant = Color(0xFF2C2C2E),
        background = Color.Black,
        onSurface = Color.White,
        onBackground = Color.White,
        secondaryContainer = primary.copy(alpha = 0.24f),
    )
} else {
    val onPrimary = readableOn(primary)
    lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = primary.copy(alpha = 0.85f),
        tertiary = primary.copy(alpha = 0.7f),
        primaryContainer = primary.copy(alpha = if (primary.luminance() < 0.35f) 0.28f else 0.18f),
        onPrimaryContainer = if (primary.luminance() < 0.35f) Color.White else Color(0xFF1C1B1F),
        surface = Color.White,
        surfaceVariant = Color(0xFFEDE7EA),
        background = Color(0xFFF8F5F7),
        onSurface = Color(0xFF1C1B1F),
        onBackground = Color(0xFF1C1B1F),
        secondaryContainer = primary.copy(alpha = 0.16f),
    )
}

private fun readableOn(color: Color): Color =
    if (color.luminance() < 0.45f) Color.White else Color(0xFF111111)
