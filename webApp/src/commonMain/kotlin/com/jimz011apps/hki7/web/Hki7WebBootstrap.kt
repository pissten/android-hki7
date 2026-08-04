package com.jimz011apps.hki7.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jimz011apps.hki7.sharedui.HKI7SharedTheme
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors

/**
 * Temporary migration surface used only to verify the browser target and deployment pipeline.
 * The production web UI will call the same shared HKI 7 composables as Android; this is not a
 * replacement or redesign of the application UI.
 */
@Composable
fun Hki7WebBootstrap() {
    HKI7SharedTheme(themeColor = "rose", themeMode = "dark") {
        val appColors = LocalHKIAppColors.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.72f),
                shape = RoundedCornerShape(28.dp),
                color = appColors.surface,
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "HKI 7",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.onSurface,
                    )
                    Text(
                        text = "Compose Multiplatform web target",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.accent,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "The browser runtime is ready. The next migration step moves the existing HKI 7 composables and platform services into shared code.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = appColors.onSurface,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MigrationStatus("WebAssembly", "Configured", Modifier.weight(1f))
                        MigrationStatus("Android app", "Unchanged", Modifier.weight(1f))
                        MigrationStatus("Shared UI", "Theme active", Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MigrationStatus(title: String, value: String, modifier: Modifier = Modifier) {
    val appColors = LocalHKIAppColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = appColors.elevated,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = appColors.onMuted)
            Spacer(Modifier.height(5.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = appColors.onSurface,
            )
        }
    }
}
