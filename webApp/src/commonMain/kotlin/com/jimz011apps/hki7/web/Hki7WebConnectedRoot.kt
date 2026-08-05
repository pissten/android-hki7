package com.jimz011apps.hki7.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.jimz011apps.hki7.data.HKIRoomWidget
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.sharedui.ha.Hki7HomeAssistantSession
import com.jimz011apps.hki7.ui.Screen
import com.jimz011apps.hki7.ui.entitiesByAreaId
import com.jimz011apps.hki7.ui.localizedTitle
import com.jimz011apps.hki7.ui.resolveAreaConfigs
import com.jimz011apps.hki7.ui.components.HKITopLevelNavigationBar
import com.jimz011apps.hki7.ui.screens.HKIRoomsSurface

/**
 * First real shared-app root for the browser target.
 *
 * The entity-list migration harness is deliberately not used here. This root renders the canonical
 * Rooms surface and bottom navigation from sharedUi. More original top-level screens are added to
 * this same root as they move out of the Android module.
 */
@Composable
fun Hki7WebConnectedRoot(
    session: Hki7HomeAssistantSession,
    serverUrl: String,
) {
    val appColors = LocalHKIAppColors.current
    val entities by session.entities.collectAsState()
    val areas by session.areas.collectAsState()
    val floors by session.floors.collectAsState()
    val entityRegistry by session.entityRegistry.collectAsState()
    val deviceRegistry by session.deviceRegistry.collectAsState()

    val areaEntities = remember(entities, entityRegistry, deviceRegistry) {
        entitiesByAreaId(
            entities = entities.values,
            entityRegistry = entityRegistry,
            deviceRegistry = deviceRegistry,
        )
    }
    val configs = remember(areas, areaEntities) {
        resolveAreaConfigs(
            areas = areas,
            entitiesByArea = areaEntities,
        )
    }
    val widgetsByArea = remember(areas) {
        areas.associate { area -> area.area_id to emptyList<HKIRoomWidget>() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background),
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            HKIRoomsSurface(
                areas = areas,
                floors = floors,
                entities = entities.values,
                entityRegistry = entityRegistry,
                deviceRegistry = deviceRegistry,
                configs = configs,
                widgetsByArea = widgetsByArea,
                baseUrl = serverUrl.removeSuffix("/"),
                onOpenArea = { /* RoomDetail is the next canonical screen being extracted. */ },
            )
        }
        HKITopLevelNavigationBar(
            screens = listOf(Screen.Rooms),
            isSelected = { true },
            onSelect = {},
            labelFor = { screen -> screen.localizedTitle() },
        )
    }
}
