package com.jimz011apps.hki7.sharedui.ha

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.websocket.WebSockets

internal actual fun createPlatformHomeAssistantHttpClient(): HttpClient =
    HttpClient(Js) {
        install(WebSockets)
    }
