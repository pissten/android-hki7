package com.jimz011apps.hki7.sharedui.ha

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets

internal actual fun createPlatformHomeAssistantHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        install(WebSockets)
    }
