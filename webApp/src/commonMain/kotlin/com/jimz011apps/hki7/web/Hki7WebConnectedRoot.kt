package com.jimz011apps.hki7.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.jimz011apps.hki7.ui.screens.HKIRoomDetailSurface
import com.jimz011apps.hki7.ui.screens.HKIRoomsSurface
import org.jetbrains.compose.resources.pluralStringResource

/**
 * Browser host for HKI 7's canonical shared Compose screens.
 *
 * Route state lives in the browser host, while page chrome, room cards, room detail and top-level
 * navigation are shared UI. No browser-only copy of the production dashboard is introduced here.
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

    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Rooms) }
    var selectedAreaId by remember { mutableStateOf<String?>(null) }

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
    val selectedArea = remember(selectedAreaId, areas) {
        selectedAreaId?.let { areaId -> areas.firstOrNull { it.area_id == areaId } }
    }

    if (selectedArea != null) {
        HKIRoomDetailSurface(
            area = selectedArea,
            entities = areaEntities[selectedArea.area_id].orEmpty(),
            baseUrl = serverUrl.removeSuffix("/"),
            onBack = { selectedAreaId = null },
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background),
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            if (selectedScreen == Screen.Rooms) {
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
                        onOpenArea = { areaId -> selectedAreaId = areaId },
                    )
                }
            } else {
                HKIPendingSharedRoute(
                    screen = selectedScreen,
                    onOpenRooms = { selectedScreen = Screen.Rooms },
                )
            }
        }
        HKITopLevelNavigationBar(
            screens = navigationScreens,
            isSelected = { screen -> screen == selectedScreen },
            onSelect = { screen ->
                selectedAreaId = null
                selectedScreen = screen
            },
            labelFor = { screen -> screen.localizedTitle() },
        )
    }
}

/**
 * Honest temporary route while each remaining Android screen is extracted into shared Compose.
 * It exists only to make route selection testable; it is not a replacement design for that screen.
 */
@Composable
private fun HKIPendingSharedRoute(
    screen: Screen,
    onOpenRooms: () -> Unit,
) {
    val appColors = LocalHKIAppColors.current
    HKISharedPage(
        title = screen.localizedTitle(),
        subtitle = "Shared Compose route",
        leftPill = {
            HKIHeaderStatusPill(text = "HKI 7")
        },
        rightPill = {
            HKIHeaderStatusPill(text = "Home Assistant")
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Denne Android-skjermen er ikke flyttet til felleskode ennå.",
                    style = MaterialTheme.typography.titleMedium,
                    color = appColors.onSurface,
                )
                Text(
                    text = "Navigasjonen er aktiv; produksjonsflaten erstatter denne meldingen i neste uttrekk.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.onMuted,
                )
                TextButton(onClick = onOpenRooms) {
                    Text("Tilbake til Rooms")
                }
            }
        }
    }
}
