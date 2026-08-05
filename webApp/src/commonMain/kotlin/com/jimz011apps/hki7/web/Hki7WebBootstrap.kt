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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jimz011apps.hki7.data.HAEntity
import com.jimz011apps.hki7.sharedui.HKI7SharedTheme
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.sharedui.ha.Hki7ConnectionStatus
import com.jimz011apps.hki7.sharedui.ha.Hki7HomeAssistantSession
import kotlinx.coroutines.launch

/**
 * Functional migration harness. It already uses the shared HKI 7 theme and the canonical
 * Home Assistant entity model. It is removed when the existing Android root composable has
 * completed its move into sharedUi; it must never evolve into a second dashboard.
 */
@Composable
fun Hki7WebBootstrap() {
    HKI7SharedTheme(themeColor = "rose", themeMode = "dark") {
        val appColors = LocalHKIAppColors.current
        val scope = rememberCoroutineScope()
        val session = remember(scope) { Hki7HomeAssistantSession(scope) }
        val status by session.status.collectAsState()
        val entities by session.entities.collectAsState()
        val error by session.error.collectAsState()
        var serverUrl by remember { mutableStateOf("") }
        var accessToken by remember { mutableStateOf("") }

        val busy = status == Hki7ConnectionStatus.CONNECTING ||
            status == Hki7ConnectionStatus.AUTHENTICATING
        val connected = status == Hki7ConnectionStatus.CONNECTED

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.background),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 980.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "HKI 7",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.onSurface,
                )
                Text(
                    text = "Compose Multiplatform migration",
                    style = MaterialTheme.typography.titleMedium,
                    color = appColors.accent,
                )

                ConnectionCard(
                    serverUrl = serverUrl,
                    onServerUrlChanged = { serverUrl = it },
                    accessToken = accessToken,
                    onAccessTokenChanged = { accessToken = it },
                    status = status,
                    error = error,
                    entityCount = entities.size,
                    busy = busy,
                    connected = connected,
                    onConnect = {
                        scope.launch {
                            runCatching { session.connect(serverUrl, accessToken) }
                        }
                    },
                    onDisconnect = {
                        scope.launch { session.disconnect() }
                    },
                )

                if (connected) {
                    Text(
                        text = "Live entities",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = appColors.onSurface,
                    )
                    Text(
                        text = "This live state stream is the shared data layer that the existing HKI 7 cards and dialogs are being moved onto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.onMuted,
                    )

                    entities.values
                        .sortedWith(
                            compareBy<HAEntity>(
                                { it.entity_id.substringBefore('.') },
                                { it.friendlyName ?: it.entity_id },
                            ),
                        )
                        .take(60)
                        .forEach { entity ->
                            EntityStateCard(
                                entity = entity,
                                onToggle = {
                                    scope.launch {
                                        val service = if (entity.state == "on") "turn_off" else "turn_on"
                                        runCatching {
                                            session.callService(
                                                domain = entity.entity_id.substringBefore('.'),
                                                service = service,
                                                entityId = entity.entity_id,
                                            )
                                        }
                                    }
                                },
                            )
                        }

                    if (entities.size > 60) {
                        Text(
                            text = "Showing 60 of ${entities.size} entities in the migration harness.",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.onMuted,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    serverUrl: String,
    onServerUrlChanged: (String) -> Unit,
    accessToken: String,
    onAccessTokenChanged: (String) -> Unit,
    status: Hki7ConnectionStatus,
    error: String?,
    entityCount: Int,
    busy: Boolean,
    connected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val appColors = LocalHKIAppColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = appColors.surface,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Home Assistant connection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = appColors.onSurface,
            )
            Text(
                text = when (status) {
                    Hki7ConnectionStatus.DISCONNECTED -> "Not connected"
                    Hki7ConnectionStatus.CONNECTING -> "Opening websocket…"
                    Hki7ConnectionStatus.AUTHENTICATING -> "Authenticating…"
                    Hki7ConnectionStatus.CONNECTED -> "Connected · $entityCount entities"
                    Hki7ConnectionStatus.ERROR -> "Connection failed"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (connected) appColors.accent else appColors.onMuted,
            )

            OutlinedTextField(
                value = serverUrl,
                onValueChange = onServerUrlChanged,
                label = { Text("Home Assistant URL") },
                placeholder = { Text("https://homeassistant.example.com") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !connected && !busy,
                singleLine = true,
            )
            OutlinedTextField(
                value = accessToken,
                onValueChange = onAccessTokenChanged,
                label = { Text("Long-lived access token") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !connected && !busy,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    busy -> CircularProgressIndicator()
                    connected -> TextButton(onClick = onDisconnect) { Text("Disconnect") }
                    else -> Button(
                        onClick = onConnect,
                        enabled = serverUrl.isNotBlank() && accessToken.isNotBlank(),
                    ) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}

@Composable
private fun EntityStateCard(
    entity: HAEntity,
    onToggle: () -> Unit,
) {
    val appColors = LocalHKIAppColors.current
    val domain = entity.entity_id.substringBefore('.')
    val canToggle = domain in setOf(
        "light",
        "switch",
        "input_boolean",
        "fan",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = appColors.subtleSurface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = entity.friendlyName
                    ?: entity.entity_id.substringAfter('.').replace('_', ' ').replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = appColors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${entity.entity_id} · ${entity.state}",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.onMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (canToggle) {
                OutlinedButton(onClick = onToggle) {
                    Text(if (entity.state == "on") "Turn off" else "Turn on")
                }
            }
        }
    }
}
