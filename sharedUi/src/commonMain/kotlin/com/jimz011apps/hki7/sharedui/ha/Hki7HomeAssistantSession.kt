package com.jimz011apps.hki7.sharedui.ha

import com.jimz011apps.hki7.data.HAArea
import com.jimz011apps.hki7.data.HADeviceRegistryEntry
import com.jimz011apps.hki7.data.HAEntity
import com.jimz011apps.hki7.data.HAEntityRegistryEntry
import com.jimz011apps.hki7.data.HAFloor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

enum class Hki7ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    AUTHENTICATING,
    CONNECTED,
    ERROR,
}

/**
 * Shared Home Assistant WebSocket session used by both browser and Android targets.
 *
 * The session exposes the canonical HKI 7 models used by the existing screens. There is
 * deliberately no browser-only entity or registry model: ported cards and dialogs consume the same
 * runtime objects as Android.
 */
class Hki7HomeAssistantSession(
    private val scope: CoroutineScope,
    private val client: HttpClient = createPlatformHomeAssistantHttpClient(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val _status = MutableStateFlow(Hki7ConnectionStatus.DISCONNECTED)
    val status: StateFlow<Hki7ConnectionStatus> = _status.asStateFlow()

    private val _entities = MutableStateFlow<Map<String, HAEntity>>(emptyMap())
    val entities: StateFlow<Map<String, HAEntity>> = _entities.asStateFlow()

    private val _areas = MutableStateFlow<List<HAArea>>(emptyList())
    val areas: StateFlow<List<HAArea>> = _areas.asStateFlow()

    private val _floors = MutableStateFlow<List<HAFloor>>(emptyList())
    val floors: StateFlow<List<HAFloor>> = _floors.asStateFlow()

    private val _entityRegistry = MutableStateFlow<List<HAEntityRegistryEntry>>(emptyList())
    val entityRegistry: StateFlow<List<HAEntityRegistryEntry>> = _entityRegistry.asStateFlow()

    private val _deviceRegistry = MutableStateFlow<List<HADeviceRegistryEntry>>(emptyList())
    val deviceRegistry: StateFlow<List<HADeviceRegistryEntry>> = _deviceRegistry.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val commandMutex = Mutex()
    private val pendingMutex = Mutex()
    private val pending = mutableMapOf<Int, CompletableDeferred<JsonObject>>()
    private var nextCommandId = 1
    private var socket: DefaultClientWebSocketSession? = null
    private var readerJob: Job? = null

    suspend fun connect(serverUrl: String, accessToken: String) {
        require(serverUrl.isNotBlank()) { "Home Assistant URL is required" }
        require(accessToken.isNotBlank()) { "Access token is required" }

        disconnect()
        _error.value = null
        _status.value = Hki7ConnectionStatus.CONNECTING

        try {
            val session = client.webSocketSession {
                url(homeAssistantWebSocketUrl(serverUrl))
            }
            socket = session

            val authRequired = receiveJsonObject(session)
            check(authRequired["type"]?.jsonPrimitive?.contentOrNull == "auth_required") {
                "Home Assistant did not start the authentication handshake"
            }

            _status.value = Hki7ConnectionStatus.AUTHENTICATING
            session.send(
                Frame.Text(
                    buildJsonObject {
                        put("type", "auth")
                        put("access_token", accessToken)
                    }.toString()
                )
            )

            val authResult = receiveJsonObject(session)
            when (authResult["type"]?.jsonPrimitive?.contentOrNull) {
                "auth_ok" -> Unit
                "auth_invalid" -> error(
                    authResult["message"]?.jsonPrimitive?.contentOrNull
                        ?: "Home Assistant rejected the access token"
                )
                else -> error("Unexpected authentication response from Home Assistant")
            }

            readerJob = scope.launch { readLoop(session) }

            val statesResponse = sendCommand("get_states")
            requireSuccess(statesResponse, "get_states")
            val stateElements = statesResponse["result"] as? JsonArray ?: JsonArray(emptyList())
            val states = json.decodeFromJsonElement(
                ListSerializer(HAEntity.serializer()),
                stateElements,
            )
            _entities.value = states.associateBy(HAEntity::entity_id)

            val areasResponse = sendCommand("config/area_registry/list")
            requireSuccess(areasResponse, "config/area_registry/list")
            val areaElements = areasResponse["result"] as? JsonArray ?: JsonArray(emptyList())
            _areas.value = json.decodeFromJsonElement(
                ListSerializer(HAArea.serializer()),
                areaElements,
            )

            val floorsResponse = sendCommand("config/floor_registry/list")
            requireSuccess(floorsResponse, "config/floor_registry/list")
            val floorElements = floorsResponse["result"] as? JsonArray ?: JsonArray(emptyList())
            _floors.value = json.decodeFromJsonElement(
                ListSerializer(HAFloor.serializer()),
                floorElements,
            )

            val entityRegistryResponse = sendCommand("config/entity_registry/list")
            requireSuccess(entityRegistryResponse, "config/entity_registry/list")
            val entityRegistryElements = entityRegistryResponse["result"] as? JsonArray
                ?: JsonArray(emptyList())
            _entityRegistry.value = json.decodeFromJsonElement(
                ListSerializer(HAEntityRegistryEntry.serializer()),
                entityRegistryElements,
            )

            val deviceRegistryResponse = sendCommand("config/device_registry/list")
            requireSuccess(deviceRegistryResponse, "config/device_registry/list")
            val deviceRegistryElements = deviceRegistryResponse["result"] as? JsonArray
                ?: JsonArray(emptyList())
            _deviceRegistry.value = json.decodeFromJsonElement(
                ListSerializer(HADeviceRegistryEntry.serializer()),
                deviceRegistryElements,
            )

            val subscriptionResponse = sendCommand(
                type = "subscribe_events",
                payload = mapOf("event_type" to JsonPrimitive("state_changed")),
            )
            requireSuccess(subscriptionResponse, "subscribe_events")
            _status.value = Hki7ConnectionStatus.CONNECTED
        } catch (error: Throwable) {
            closeConnection(clearEntities = false)
            _error.value = error.message ?: error::class.simpleName ?: "Connection failed"
            _status.value = Hki7ConnectionStatus.ERROR
            throw error
        }
    }

    suspend fun callService(
        domain: String,
        service: String,
        entityId: String? = null,
        serviceData: JsonObject = JsonObject(emptyMap()),
    ): JsonObject {
        check(_status.value == Hki7ConnectionStatus.CONNECTED) {
            "Home Assistant is not connected"
        }
        val response = sendCommand(
            type = "call_service",
            payload = buildMap {
                put("domain", JsonPrimitive(domain))
                put("service", JsonPrimitive(service))
                put("service_data", serviceData)
                if (!entityId.isNullOrBlank()) {
                    put(
                        "target",
                        buildJsonObject { put("entity_id", entityId) },
                    )
                }
                put("return_response", JsonPrimitive(false))
            },
        )
        requireSuccess(response, "$domain.$service")
        return response
    }

    suspend fun disconnect() {
        closeConnection(clearEntities = false)
        _status.value = Hki7ConnectionStatus.DISCONNECTED
        _error.value = null
    }

    suspend fun dispose() {
        closeConnection(clearEntities = true)
        client.close()
    }

    private suspend fun sendCommand(
        type: String,
        payload: Map<String, JsonElement> = emptyMap(),
    ): JsonObject {
        val activeSocket = socket ?: error("Home Assistant websocket is not connected")
        val response = CompletableDeferred<JsonObject>()
        val id = commandMutex.withLock { nextCommandId++ }
        pendingMutex.withLock { pending[id] = response }

        val request = buildJsonObject {
            put("id", id)
            put("type", type)
            payload.forEach { (key, value) -> put(key, value) }
        }

        return try {
            activeSocket.send(Frame.Text(request.toString()))
            withTimeout(20_000L) { response.await() }
        } finally {
            pendingMutex.withLock { pending.remove(id) }
        }
    }

    private suspend fun readLoop(session: DefaultClientWebSocketSession) {
        try {
            while (scope.isActive && !session.incoming.isClosedForReceive) {
                val frame = session.incoming.receive()
                if (frame !is Frame.Text) continue
                val message = json.parseToJsonElement(frame.readText()).jsonObject
                when (message["type"]?.jsonPrimitive?.contentOrNull) {
                    "result" -> {
                        val id = message["id"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                        if (id != null) {
                            pendingMutex.withLock { pending.remove(id) }?.complete(message)
                        }
                    }
                    "event" -> handleEvent(message)
                }
            }
        } catch (error: Throwable) {
            if (_status.value != Hki7ConnectionStatus.DISCONNECTED) {
                _error.value = error.message ?: "Home Assistant websocket closed"
                _status.value = Hki7ConnectionStatus.ERROR
            }
        } finally {
            failPendingCommands("Home Assistant websocket closed")
        }
    }

    private fun handleEvent(message: JsonObject) {
        val event = message["event"] as? JsonObject ?: return
        if (event["event_type"]?.jsonPrimitive?.contentOrNull != "state_changed") return
        val data = event["data"] as? JsonObject ?: return
        val entityId = data["entity_id"]?.jsonPrimitive?.contentOrNull ?: return
        val newStateElement = data["new_state"]

        if (newStateElement == null || newStateElement is JsonNull) {
            _entities.update { current -> current - entityId }
            return
        }

        val newState = runCatching {
            json.decodeFromJsonElement(HAEntity.serializer(), newStateElement)
        }.getOrNull() ?: return
        _entities.update { current -> current + (entityId to newState) }
    }

    private suspend fun closeConnection(clearEntities: Boolean) {
        val oldReader = readerJob
        readerJob = null
        oldReader?.cancelAndJoin()
        runCatching { socket?.close() }
        socket = null
        failPendingCommands("Home Assistant connection closed")
        if (clearEntities) {
            _entities.value = emptyMap()
            _areas.value = emptyList()
            _floors.value = emptyList()
            _entityRegistry.value = emptyList()
            _deviceRegistry.value = emptyList()
        }
    }

    private suspend fun failPendingCommands(message: String) {
        val waiting = pendingMutex.withLock {
            pending.values.toList().also { pending.clear() }
        }
        waiting.forEach { it.completeExceptionally(IllegalStateException(message)) }
    }

    private suspend fun receiveJsonObject(session: DefaultClientWebSocketSession): JsonObject {
        while (!session.incoming.isClosedForReceive) {
            val frame = session.incoming.receive()
            if (frame is Frame.Text) {
                return json.parseToJsonElement(frame.readText()).jsonObject
            }
        }
        error("Home Assistant closed the websocket during authentication")
    }

    private fun requireSuccess(response: JsonObject, operation: String) {
        if (response["success"]?.jsonPrimitive?.booleanOrNull == true) return
        val errorObject = response["error"] as? JsonObject
        val message = errorObject?.get("message")?.jsonPrimitive?.contentOrNull
            ?: errorObject?.get("code")?.jsonPrimitive?.contentOrNull
            ?: "Unknown Home Assistant error"
        error("$operation failed: $message")
    }
}

internal expect fun createPlatformHomeAssistantHttpClient(): HttpClient

fun homeAssistantWebSocketUrl(serverUrl: String): String {
    val normalized = serverUrl.trim().removeSuffix("/")
    val socketBase = when {
        normalized.startsWith("wss://", ignoreCase = true) -> normalized
        normalized.startsWith("ws://", ignoreCase = true) -> normalized
        normalized.startsWith("https://", ignoreCase = true) ->
            "wss://${normalized.substringAfter("://")}" 
        normalized.startsWith("http://", ignoreCase = true) ->
            "ws://${normalized.substringAfter("://")}" 
        else -> "ws://$normalized"
    }
    return "$socketBase/api/websocket"
}
