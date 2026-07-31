package com.jimz011apps.hki7.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.ui.theme.LocalHKIAppColors

private const val ORIENTATION_PREFS = "hki7_display_preferences"
private const val ORIENTATION_KEY = "screen_orientation"

const val ORIENTATION_PORTRAIT = "portrait"
const val ORIENTATION_LANDSCAPE = "landscape"
const val ORIENTATION_AUTO = "auto"

fun Context.savedScreenOrientation(): String =
    getSharedPreferences(ORIENTATION_PREFS, Context.MODE_PRIVATE)
        .getString(ORIENTATION_KEY, ORIENTATION_PORTRAIT)
        ?.takeIf { it == ORIENTATION_PORTRAIT || it == ORIENTATION_LANDSCAPE || it == ORIENTATION_AUTO }
        ?: ORIENTATION_PORTRAIT

fun Activity.applySavedScreenOrientation() {
    requestedOrientation = when (savedScreenOrientation()) {
        ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        ORIENTATION_AUTO -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

private fun Context.findHostActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}

private fun saveScreenOrientation(context: Context, value: String) {
    context.getSharedPreferences(ORIENTATION_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(ORIENTATION_KEY, value)
        .apply()
    context.findHostActivity()?.applySavedScreenOrientation()
}

@Composable
fun ScreenOrientationSettingsCard() {
    val context = LocalContext.current
    val colors = LocalHKIAppColors.current
    var selected by remember { mutableStateOf(context.savedScreenOrientation()) }

    SettingsSubcategory(
        title = "Screen orientation",
        description = "Choose how HKI 7 uses the display"
    )
    SettingsGroup {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(Modifier.weight(1f)) {
                Text("Orientation", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(
                    when (selected) {
                        ORIENTATION_LANDSCAPE -> "Always use landscape"
                        ORIENTATION_AUTO -> "Follow the tablet's auto-rotate setting"
                        else -> "Always use portrait"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onMuted
                )
            }
        }

        SettingsTabRow(
            tabs = listOf(
                ORIENTATION_PORTRAIT to "Portrait",
                ORIENTATION_LANDSCAPE to "Landscape",
                ORIENTATION_AUTO to "Auto-rotate"
            ),
            selected = selected,
            onSelect = { value ->
                selected = value
                saveScreenOrientation(context, value)
            }
        )
    }
}
