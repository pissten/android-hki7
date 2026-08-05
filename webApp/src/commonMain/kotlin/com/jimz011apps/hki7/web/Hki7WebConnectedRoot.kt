package com.jimz011apps.hki7.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.jimz011apps.hki7.data.HKIAreaConfig
import com.jimz011apps.hki7.data.HKIRoomWidget
import com.jimz011apps.hki7.resources.Res
import com.jimz011apps.hki7.resources.rooms_count
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.sharedui.ha.Hki7HomeAssistantSession
import com.jimz011apps.hki7.ui.NavBarConfig
import com.jimz011apps.hki7.ui.Screen
import com.jimz011apps.hki7.ui.displayedRoomControlEntityIds
import com.jimz011apps.hki7.ui.entitiesByAreaId
import com.jimz011apps.hki7.ui.localizedTitle
import com.jimz011apps.hki7.ui.resolveAreaConfigs
import com.jimz011apps.hki7.ui.resolveWholeHomeStatus
import com.jimz011apps.hki7.ui.components.HKIHeaderStatusPill
import com.jimz011apps.hki7.ui.components.HKISharedPage
import com.jimz011apps.hki7.ui.components.HKITopLevelNavigationBar
import com.jimz011apps.hki7.ui.components.RoomStatusIndicators
import com.jimz011apps.hki7.ui.screens.HKIRoomsSurface
import org.jetbrains.compose.resources.pluralStringResource

/**
 * Browser host for the canonical shared HKI 7 Rooms screen.
 *
 * The page header, floor layout, room cards and bottom navigation are shared Compose components,
 * not separately styled browser copies. Unported tabs stay visible in the canonical order so the
 * first visual-parity test includes the real application chrome without pretending those screens
 * are already functional.
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
    val activeConfigs = remember(areas, configs) {
        areas.map { area -> configs[area.area_id] ?: HKIAreaConfig() }
    }
    val wholeHomeDisplayedControlIds = remember(areas, widgetsByArea) {
        areas
            .flatMap { area -> displayedRoomControlEntityIds(widgetsByArea[area.area_id].orEmpty()) }
            .toSet()
    }
    val wholeHomeSummary = remember(activeConfigs, entities, wholeHomeDisplayedControlIds) {
        resolveWholeHomeStatus(
            configs = activeConfigs,
            entities = entities.values.toList(),
            displayedControlEntityIds = wholeHomeDisplayedControlIds,
        )
    }
    val roomsSubtitle = wholeHomeSummary.environmentText
        ?: pluralStringResource(Res.plurals.rooms_count, areas.size, areas.size)
    val navigationScreens = remember { NavBarConfig.visibleTabs(emptyList(), emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background),
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            HKISharedPage(
                title = Screen.Rooms.localizedTitle(),
                subtitle = roomsSubtitle,
                leftPill = {
                    HKIHeaderStatusPill(text = "HKI 7")
                },
                rightPill = {
                    HKIHeaderStatusPill(text = "Home Assistant")
                },
                headerTrailingContent = if (wholeHomeSummary.indicators.isNotEmpty()) {
                    { _ ->
                        RoomStatusIndicators(
                            summary = wholeHomeSummary,
                            compact = false,
                        )
                    }
                } else {
                    null
                },
            ) {
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
        }
        HKITopLevelNavigationBar(
            screens = navigationScreens,
            isSelected = { screen -> screen == Screen.Rooms },
            onSelect = { /* Only Rooms is enabled in this visual-parity preview. */ },
            labelFor = { screen -> screen.localizedTitle() },
        )
    }
}
