package com.jimz011apps.hki7.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.ui.graphics.vector.ImageVector
import com.jimz011apps.hki7.data.HKICustomPage

/**
 * HKI 7's real top-level navigation model, shared by Android and web.
 *
 * Platform-specific navigation hosts render these exact routes and icons. Keeping the model in
 * commonMain prevents the web client from inventing a parallel set of screens or tab ordering.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val mdiIcon: String? = null,
) {
    companion object {
        const val CUSTOM_PAGE_ROUTE = "custom_page/{pageId}"
    }

    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Rooms : Screen("rooms", "Rooms", Icons.AutoMirrored.Filled.ViewQuilt)
    data object Security : Screen("security", "Security", Icons.Default.Security)
    data object Energy : Screen("energy", "Energy", Icons.Default.ElectricBolt)
    data object Climate : Screen("climate", "Climate", Icons.Default.Thermostat, mdiIcon = "thermostat")
    data object Battery : Screen("battery", "Battery", Icons.Default.BatteryAlert) {
        const val WIDGET_ROUTE = "battery/widget"
    }
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    data class Custom(val page: HKICustomPage) : Screen(
        route = "custom_page/${page.id}",
        title = page.name,
        icon = Icons.Default.Dashboard,
        mdiIcon = page.icon,
    )

    data object RoomDetail : Screen(
        "room_detail/{areaId}",
        "Room Detail",
        Icons.AutoMirrored.Filled.ViewQuilt,
    ) {
        fun createRoute(areaId: String): String = "room_detail/$areaId"
    }
}

/** Maps an HKI action navigation target to the exact route used by the real app. */
fun navRouteForTarget(target: String): String? = when {
    target == "home" -> Screen.Home.route
    target == "rooms" -> Screen.Rooms.route
    target == "security" -> Screen.Security.route
    target == "energy" -> Screen.Energy.route
    target == "climate" -> Screen.Climate.route
    target == "battery" -> Screen.Battery.route
    target == "settings" -> Screen.Settings.route
    target.startsWith("room:") -> Screen.RoomDetail.createRoute(target.removePrefix("room:"))
    else -> null
}

/** The canonical bottom-navigation ordering and visibility rules used by Android and web. */
object NavBarConfig {
    val fixed: List<Screen> = listOf(Screen.Home, Screen.Rooms)
    val configurable: List<Screen> = listOf(
        Screen.Climate,
        Screen.Security,
        Screen.Energy,
        Screen.Battery,
    )

    fun orderedConfigurable(
        savedOrder: List<String>,
        customPages: List<HKICustomPage> = emptyList(),
    ): List<Screen> {
        val available = configurable + customPages.map(Screen::Custom)
        val byRoute = available.associateBy { it.route }
        val ordered = savedOrder.mapNotNull { byRoute[it] }
        val rest = available.filter { it !in ordered }
        return ordered + rest
    }

    fun visibleTabs(
        savedOrder: List<String>,
        hidden: List<String>,
        customPages: List<HKICustomPage> = emptyList(),
    ): List<Screen> {
        val hiddenSet = hidden.toSet()
        return fixed + orderedConfigurable(savedOrder, customPages)
            .filter { it.route !in hiddenSet }
    }
}
