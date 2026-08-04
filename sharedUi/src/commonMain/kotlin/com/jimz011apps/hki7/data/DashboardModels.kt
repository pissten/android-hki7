package com.jimz011apps.hki7.data

import kotlinx.serialization.Serializable

/**
 * Complete persisted HKI 7 dashboard definition shared by Android and browser clients.
 * Keeping this model in common code lets both platforms decode the same backups, shared
 * dashboards, room/widget structure, navigation settings, and header configuration.
 */
@Serializable
data class HKIDashboard(
    val id: String,
    val name: String,
    val mode: String = "manual",
    val areaOrder: List<String> = emptyList(),
    val areas: List<HAArea> = emptyList(),
    val floors: List<HAFloor> = emptyList(),
    val areaWidgets: Map<String, List<HKIRoomWidget>> = emptyMap(),
    val areaConfigs: Map<String, HKIAreaConfig> = emptyMap(),
    val pageConfigs: Map<String, HKIPageConfig> = emptyMap(),
    val customPages: List<HKICustomPage> = emptyList(),
    /** Popup dialogs this dashboard's buttons/badges can open; their widgets live in [areaWidgets]
     * under [customPopupWidgetAreaId]. */
    val customPopups: List<HKICustomPopup> = emptyList(),
    val navBarOrder: List<String> = emptyList(),
    val navBarHidden: List<String> = emptyList(),
    // Per-dashboard media-player bar config, so each (shared) dashboard carries its own selection
    // instead of one global list everyone must curate.
    val mediaPlayerNames: Map<String, String> = emptyMap(),
    val mediaPlayerBarHidden: List<String> = emptyList(),
    // For an imported shared dashboard ("shared-<id>"): the source's `updated` timestamp last merged
    // in, so auto-update can skip re-fetching an unchanged shared dashboard.
    val sharedUpdatedAt: String? = null,
    /** Header pill settings (weather/alarm/date-time display + their linked entities and rain map).
     * These are otherwise global, so bundling them per-dashboard lets them travel with family sharing.
     * Null on dashboards created before this existed — global prefs are then left untouched. */
    val headerPill: HeaderPillConfig? = null,
)

/** The header display pills (left + right) and their linked entities, captured per-dashboard so they
 * carry across cloud/family sharing (they are otherwise global preferences). */
@Serializable
data class HeaderPillConfig(
    val rightDisplayType: String = "Weather",
    val leftDisplayType: String = "None",
    /** Weather role -> entity id / value: sun, moon, aqi, season, rain, device, rainmap, rainmap_url,
     *  rainmap_aspect. */
    val weatherEntities: Map<String, String> = emptyMap(),
    val rightAlarmEntityIds: List<String> = emptyList(),
    val leftAlarmEntityIds: List<String> = emptyList(),
)
