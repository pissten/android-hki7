package com.jimz011apps.hki7.ui

import com.jimz011apps.hki7.data.HAArea
import com.jimz011apps.hki7.data.HADeviceRegistryEntry
import com.jimz011apps.hki7.data.HAEntity
import com.jimz011apps.hki7.data.HAEntityRegistryEntry
import com.jimz011apps.hki7.data.HKIAreaConfig

/** Resolves Home Assistant entities to their effective area, including device-inherited area ids. */
fun entitiesByAreaId(
    entities: Collection<HAEntity>,
    entityRegistry: List<HAEntityRegistryEntry>,
    deviceRegistry: List<HADeviceRegistryEntry>,
): Map<String, List<HAEntity>> {
    val deviceAreaById = deviceRegistry.associate { device -> device.id to device.area_id }
    val areaByEntityId = entityRegistry.associate { entry ->
        entry.entity_id to (entry.area_id ?: entry.device_id?.let(deviceAreaById::get))
    }
    return entities
        .mapNotNull { entity -> areaByEntityId[entity.entity_id]?.let { areaId -> areaId to entity } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
}

/**
 * Builds the same initial room-summary configuration HKI 7 derives during automatic dashboard
 * import. Existing persisted configuration always wins; only missing rooms are discovered.
 */
fun resolveAreaConfigs(
    areas: List<HAArea>,
    entitiesByArea: Map<String, List<HAEntity>>,
    existing: Map<String, HKIAreaConfig> = emptyMap(),
): Map<String, HKIAreaConfig> = areas.associate { area ->
    val configured = existing[area.area_id]
    if (configured != null) {
        area.area_id to configured
    } else {
        val discovery = discoverRoomStatus(entitiesByArea[area.area_id].orEmpty())
        area.area_id to HKIAreaConfig(
            floorId = area.floor_id,
            roomStatusEntityIds = discovery.entityIds,
            roomTemperatureEntityIds = discovery.temperatureEntityIds,
            roomHumidityEntityIds = discovery.humidityEntityIds,
        )
    }
}
