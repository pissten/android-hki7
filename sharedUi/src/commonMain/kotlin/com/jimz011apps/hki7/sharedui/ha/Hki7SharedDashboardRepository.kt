package com.jimz011apps.hki7.sharedui.ha

import com.jimz011apps.hki7.data.HKIDashboard
import com.jimz011apps.hki7.data.Hki7SharedDashboardMeta
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val sharedDashboardJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * Reads the same shared-dashboard catalogue used by the Android client.
 *
 * The companion component returns metadata only here. The complete [HKIDashboard] is fetched on
 * selection, so web and Android decode precisely the same persisted model rather than maintaining a
 * browser-specific configuration format.
 */
suspend fun Hki7HomeAssistantSession.listSharedDashboards(): List<Hki7SharedDashboardMeta> {
    val response = sendCommand("hki7/dashboard/list")
    if (response["success"]?.jsonPrimitive?.booleanOrNull != true) return emptyList()
    val dashboards = response["result"]
        ?.let { it as? JsonObject }
        ?.get("dashboards") as? JsonArray
        ?: return emptyList()

    return dashboards.mapNotNull { element ->
        val item = element as? JsonObject ?: return@mapNotNull null
        val id = item["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        Hki7SharedDashboardMeta(
            id = id,
            ownerId = item["owner_id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            name = item["name"]?.jsonPrimitive?.contentOrNull ?: "Shared dashboard",
            updated = item["updated"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            sharedWith = (item["shared_with"] as? JsonArray)
                .orEmpty()
                .mapNotNull { value -> value.jsonPrimitive.contentOrNull },
        )
    }
}

/** Fetches and decodes one dashboard using the exact Android HKIDashboard serializer. */
suspend fun Hki7HomeAssistantSession.getSharedDashboard(dashboardId: String): HKIDashboard? {
    require(dashboardId.isNotBlank()) { "Dashboard id is required" }
    val response = sendCommand(
        type = "hki7/dashboard/get",
        payload = mapOf("dashboard_id" to kotlinx.serialization.json.JsonPrimitive(dashboardId)),
    )
    if (response["success"]?.jsonPrimitive?.booleanOrNull != true) return null
    val payload = response["result"]
        ?.let { it as? JsonObject }
        ?.get("payload") as? JsonObject
        ?: return null

    return sharedDashboardJson.decodeFromJsonElement(HKIDashboard.serializer(), payload)
}
