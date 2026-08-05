package com.jimz011apps.hki7.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jimz011apps.hki7.R
import com.jimz011apps.hki7.data.HomeAssistantConnectionRoute

/** Android resource wrapper for the platform-specific connection-route enum. */
@Composable
fun HomeAssistantConnectionRoute.localizedName(): String = when (this) {
    HomeAssistantConnectionRoute.LOCAL -> stringResource(R.string.connection_route_local)
    HomeAssistantConnectionRoute.NABU_CASA -> stringResource(R.string.connection_route_nabu_casa)
    HomeAssistantConnectionRoute.EXTERNAL -> stringResource(R.string.connection_route_external)
}
