package com.jimz011apps.hki7.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jimz011apps.hki7.R

/** Android resource-backed labels for the canonical shared navigation model. */
@Composable
fun Screen.localizedTitle(): String = when (this) {
    Screen.Home -> stringResource(R.string.nav_home)
    Screen.Rooms -> stringResource(R.string.nav_rooms)
    Screen.Security -> stringResource(R.string.nav_security)
    Screen.Energy -> stringResource(R.string.nav_energy)
    Screen.Climate -> stringResource(R.string.nav_climate)
    Screen.Battery -> stringResource(R.string.nav_battery)
    Screen.Settings -> stringResource(R.string.nav_settings)
    Screen.RoomDetail -> stringResource(R.string.nav_room_detail)
    is Screen.Custom -> page.name
}
