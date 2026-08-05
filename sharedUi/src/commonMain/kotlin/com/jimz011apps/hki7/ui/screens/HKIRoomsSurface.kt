package com.jimz011apps.hki7.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.data.HAArea
import com.jimz011apps.hki7.data.HADeviceRegistryEntry
import com.jimz011apps.hki7.data.HAEntity
import com.jimz011apps.hki7.data.HAEntityRegistryEntry
import com.jimz011apps.hki7.data.HAFloor
import com.jimz011apps.hki7.data.HKIAreaConfig
import com.jimz011apps.hki7.data.HKIRoomWidget
import com.jimz011apps.hki7.resources.Res
import com.jimz011apps.hki7.resources.floor_collapse
import com.jimz011apps.hki7.resources.floor_expand
import com.jimz011apps.hki7.resources.ui_rooms_3a28d6f
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.ui.components.HKIAreaCard
import com.jimz011apps.hki7.ui.components.LocalItemCornerRadius
import com.jimz011apps.hki7.ui.components.ReorderableGrid
import com.jimz011apps.hki7.ui.components.responsiveDashboardColumnCount
import com.jimz011apps.hki7.ui.utils.MdiIcon
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

private data class SharedFloorSection(
    val key: String,
    val floor: HAFloor?,
    val areas: List<HAArea>,
)

/**
 * Canonical HKI 7 Rooms surface for commonMain.
 *
 * This is extracted from the original Android RoomsScreen floor/card hierarchy. It renders the
 * same floor headers, responsive lanes, per-floor columns and [HKIAreaCard]. Platform hosts provide
 * only registry/config state and navigation callbacks.
 */
@Composable
fun HKIRoomsSurface(
    areas: List<HAArea>,
    floors: List<HAFloor>,
    entities: Collection<HAEntity>,
    entityRegistry: List<HAEntityRegistryEntry>,
    deviceRegistry: List<HADeviceRegistryEntry>,
    configs: Map<String, HKIAreaConfig>,
    widgetsByArea: Map<String, List<HKIRoomWidget>>,
    baseUrl: String,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onOpenArea: (String) -> Unit,
    onDeleteArea: (String) -> Unit = {},
    onConfigureArea: (String) -> Unit = {},
    onActivityClick: ((String, List<String>) -> Unit)? = null,
) {
    val appColors = LocalHKIAppColors.current
    val scrollState = rememberScrollState()
    val collapsed = remember { mutableStateMapOf<String, Boolean>() }
    val sections = remember(areas, floors, configs) {
        buildSharedFloorSections(areas, floors, configs)
    }
    val entitiesByArea = remember(entities, entityRegistry, deviceRegistry) {
        entitiesGroupedByArea(
            entities = entities,
            entityRegistry = entityRegistry,
            deviceRegistry = deviceRegistry,
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val dashboardLanes = responsiveDashboardColumnCount(maxWidth)
        val packedRows = remember(sections, dashboardLanes) {
            packSharedFloorRows(sections, maxUnits = dashboardLanes * 2)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(Res.string.ui_rooms_3a28d6f),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.onSurface,
            )

            packedRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    row.forEach { section ->
                        val spanUnits = if (section.floor?.width == "half") 1 else 2
                        SharedFloorSectionView(
                            section = section,
                            configs = configs,
                            widgetsByArea = widgetsByArea,
                            entitiesByArea = entitiesByArea,
                            baseUrl = baseUrl,
                            isEditMode = isEditMode,
                            scrollState = scrollState,
                            isCollapsed = collapsed[section.key] == true,
                            onToggleCollapsed = {
                                collapsed[section.key] = collapsed[section.key] != true
                            },
                            onOpenArea = onOpenArea,
                            onDeleteArea = onDeleteArea,
                            onConfigureArea = onConfigureArea,
                            onActivityClick = onActivityClick,
                            modifier = Modifier.weight(spanUnits.toFloat()),
                        )
                    }
                    val occupiedUnits = row.sumOf { if (it.floor?.width == "half") 1 else 2 }
                    val missingUnits = dashboardLanes * 2 - occupiedUnits
                    if (missingUnits > 0) {
                        Spacer(Modifier.weight(missingUnits.toFloat()))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SharedFloorSectionView(
    section: SharedFloorSection,
    configs: Map<String, HKIAreaConfig>,
    widgetsByArea: Map<String, List<HKIRoomWidget>>,
    entitiesByArea: Map<String, List<HAEntity>>,
    baseUrl: String,
    isEditMode: Boolean,
    scrollState: ScrollState,
    isCollapsed: Boolean,
    onToggleCollapsed: () -> Unit,
    onOpenArea: (String) -> Unit,
    onDeleteArea: (String) -> Unit,
    onConfigureArea: (String) -> Unit,
    onActivityClick: ((String, List<String>) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalHKIAppColors.current
    val latestOpenArea by rememberUpdatedState(onOpenArea)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .clickable { onToggleCollapsed() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (section.floor?.icon != "None") {
                MdiIcon(section.floor?.icon, tint = appColors.onMuted, size = 16.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = section.floor?.name ?: stringResource(Res.string.ui_rooms_3a28d6f),
                color = appColors.onMuted,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = stringResource(
                    if (isCollapsed) Res.string.floor_expand else Res.string.floor_collapse,
                ),
                tint = appColors.onMuted,
                modifier = Modifier.size(18.dp),
            )
        }

        if (!isCollapsed) {
            Spacer(Modifier.height(12.dp))
            val floor = section.floor
            val gridColumns = floor?.columns?.coerceIn(1, 3) ?: 1
            val compactTiles = floor?.compactTiles ?: true
            val cardHeight = if (compactTiles) 112 else 160
            val rowHeight = if (floor?.isSquare == true) 180 else cardHeight + 12
            val rows = max(1, (section.areas.size + gridColumns - 1) / gridColumns)

            ReorderableGrid(
                items = section.areas,
                canReorder = false,
                onReorder = { _, _ -> },
                key = HAArea::area_id,
                columns = GridCells.Fixed(gridColumns),
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                isNested = true,
                autoScrollState = scrollState,
                modifier = Modifier
                    .heightIn(min = rowHeight.dp, max = (rows * rowHeight).dp)
                    .fillMaxWidth(),
            ) { area, isDragging ->
                HKIAreaCard(
                    area = area,
                    config = configs[area.area_id] ?: HKIAreaConfig(),
                    widgets = widgetsByArea[area.area_id].orEmpty(),
                    roomEntities = entitiesByArea[area.area_id].orEmpty(),
                    peopleHere = 0,
                    baseUrl = baseUrl,
                    isEditMode = isEditMode,
                    canDelete = true,
                    isDragging = isDragging,
                    isSquare = floor?.isSquare == true,
                    compactTiles = compactTiles,
                    cornerRadius = floor?.cornerRadius ?: LocalItemCornerRadius.current,
                    onDelete = { onDeleteArea(area.area_id) },
                    onSettings = { onConfigureArea(area.area_id) },
                    onClick = { latestOpenArea(area.area_id) },
                    onActivityClick = onActivityClick,
                )
            }
        }
    }
}

private fun buildSharedFloorSections(
    areas: List<HAArea>,
    floors: List<HAFloor>,
    configs: Map<String, HKIAreaConfig>,
): List<SharedFloorSection> {
    val layoutOnlyFloor = floors.firstOrNull { it.floor_id == "__rooms__" }
    val importedFloors = floors.filterNot { it.floor_id == "__rooms__" }
    val knownFloorIds = importedFloors.map(HAFloor::floor_id).toSet()
    val byFloor = areas.groupBy { area -> configs[area.area_id]?.floorId ?: area.floor_id }
    val sections = importedFloors.map { floor ->
        SharedFloorSection(floor.floor_id, floor, byFloor[floor.floor_id].orEmpty())
    }.filter { it.areas.isNotEmpty() || importedFloors.isNotEmpty() }
    val unassigned = byFloor
        .filterKeys { it == null || it !in knownFloorIds }
        .values
        .flatten()
    return if (unassigned.isNotEmpty()) {
        sections + SharedFloorSection("__rooms__", layoutOnlyFloor, unassigned)
    } else {
        sections
    }
}

private fun packSharedFloorRows(
    sections: List<SharedFloorSection>,
    maxUnits: Int,
): List<List<SharedFloorSection>> {
    val rows = mutableListOf<List<SharedFloorSection>>()
    var current = mutableListOf<SharedFloorSection>()
    var used = 0
    for (section in sections) {
        val units = if (section.floor?.width == "half") 1 else 2
        if (used + units > maxUnits && current.isNotEmpty()) {
            rows += current.toList()
            current = mutableListOf()
            used = 0
        }
        current += section
        used += units
    }
    if (current.isNotEmpty()) rows += current.toList()
    return rows
}

private fun entitiesGroupedByArea(
    entities: Collection<HAEntity>,
    entityRegistry: List<HAEntityRegistryEntry>,
    deviceRegistry: List<HADeviceRegistryEntry>,
): Map<String, List<HAEntity>> {
    val deviceAreaById = deviceRegistry.associate { it.id to it.area_id }
    val areaByEntityId = entityRegistry.associate { entry ->
        entry.entity_id to (entry.area_id ?: entry.device_id?.let(deviceAreaById::get))
    }
    return entities
        .mapNotNull { entity -> areaByEntityId[entity.entity_id]?.let { it to entity } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
}
