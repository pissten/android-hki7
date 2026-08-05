package com.jimz011apps.hki7.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jimz011apps.hki7.sharedui.HKI7SharedTheme
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.sharedui.ha.Hki7ConnectionStatus
import com.jimz011apps.hki7.sharedui.ha.Hki7HomeAssistantSession
import kotlinx.coroutines.launch

/**
 * Temporary browser host around the shared app root.
 *
 * The old live-entity migration page has been removed. A successful connection now enters the
 * canonical shared Rooms UI directly. This host remains non-production until the original HKI 7
 * onboarding/storage flow is extracted from Android.
 */
@Composable
fun Hki7WebBootstrap() {
    HKI7SharedTheme(themeColor = "rose", themeMode = "dark") {
        val scope = rememberCoroutineScope()
        val session = remember(scope) { Hki7HomeAssistantSession(scope) }
        val status by session.status.collectAsState()
        val error by session.error.collectAsState()
        var serverUrl by remember { mutableStateOf("") }
        var accessToken by remember { mutableStateOf("") }

        if (status == Hki7ConnectionStatus.CONNECTED) {
            Hki7WebConnectedRoot(
                session = session,
                serverUrl = serverUrl,
            )
        } else {
            Hki7WebConnectionHost(
                serverUrl = serverUrl,
                onServerUrlChanged = { serverUrl = it },
                accessToken = accessToken,
                onAccessTokenChanged = { accessToken = it },
                status = status,
                error = error,
                onConnect = {
                    scope.launch {
                        runCatching { session.connect(serverUrl, accessToken) }
                    }
                },
            )
        }
    }
}

@Composable
private fun Hki7WebConnectionHost(
    serverUrl: String,
    onServerUrlChanged: (String) -> Unit,
    accessToken: String,
    onAccessTokenChanged: (String) -> Unit,
    status: Hki7ConnectionStatus,
    error: String?,
    onConnect: () -> Unit,
) {
    val appColors = LocalHKIAppColors.current
    val busy = status == Hki7ConnectionStatus.CONNECTING ||
        status == Hki7ConnectionStatus.AUTHENTICATING

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "HKI 7",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.onSurface,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = appColors.surface,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Home Assistant",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = appColors.onSurface,
                    )
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = onServerUrlChanged,
                        label = { Text("Home Assistant URL") },
                        placeholder = { Text("https://homeassistant.example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !busy,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = accessToken,
                        onValueChange = onAccessTokenChanged,
                        label = { Text("Access token") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !busy,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    error?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (busy) {
                            CircularProgressIndicator()
                        } else {
                            Button(
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
    }
}
