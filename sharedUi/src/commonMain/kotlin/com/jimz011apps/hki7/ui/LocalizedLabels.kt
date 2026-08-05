package com.jimz011apps.hki7.ui

import androidx.compose.runtime.Composable
import com.jimz011apps.hki7.data.HAEntity
import com.jimz011apps.hki7.resources.Res
import com.jimz011apps.hki7.resources.*
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.stringResource

/** Home Assistant keeps state values as stable English tokens; translate the common states at the
 * presentation boundary while leaving custom integration states intact. */
@Composable
fun HAEntity.localizedStateLabel(): String {
    val unit = attributes?.get("unit_of_measurement")?.jsonPrimitive?.contentOrNull
    state.toDoubleOrNull()?.let { value ->
        val compactValue = (kotlin.math.round(value * 10.0) / 10.0).toString()
            .trimEnd('0')
            .trimEnd('.')
        return listOfNotNull(compactValue, unit?.takeIf(String::isNotBlank))
            .joinToString(" ")
    }

    val normalized = state.trim().lowercase()
    if (entity_id.startsWith("binary_sensor.") && normalized in setOf("on", "off")) {
        val active = normalized == "on"
        return when (deviceClass) {
            "door", "garage_door", "window", "opening" ->
                stringResource(if (active) Res.string.cr_open else Res.string.cr_closed)
            "lock" -> stringResource(if (active) Res.string.cr_unlocked else Res.string.cr_locked)
            "moisture" -> stringResource(if (active) Res.string.cr_wet else Res.string.cr_dry)
            "motion", "moving", "occupancy", "presence", "smoke", "gas", "co" ->
                stringResource(if (active) Res.string.cr_detected else Res.string.cr_clear)
            "problem", "safety", "tamper" ->
                stringResource(if (active) Res.string.cr_problem else Res.string.cr_ok)
            "battery" -> stringResource(if (active) Res.string.cr_low else Res.string.cr_normal)
            "connectivity" ->
                stringResource(if (active) Res.string.cr_connected else Res.string.cr_disconnected)
            "plug", "power" ->
                stringResource(if (active) Res.string.cr_plugged_in else Res.string.cr_unplugged)
            else -> stringResource(if (active) Res.string.cr_state_on else Res.string.cr_state_off)
        }
    }

    return localizedCommonStateLabel(state)
}

/** Translate a common Home Assistant state token when no full entity/device-class context exists. */
@Composable
fun localizedCommonStateLabel(state: String): String = when (state.trim().lowercase()) {
    "on" -> stringResource(Res.string.cr_state_on)
    "off" -> stringResource(Res.string.cr_state_off)
    "open" -> stringResource(Res.string.cr_open)
    "closed" -> stringResource(Res.string.cr_closed)
    "opening" -> stringResource(Res.string.cr_opening)
    "closing" -> stringResource(Res.string.cr_closing)
    "locked" -> stringResource(Res.string.cr_locked)
    "unlocked" -> stringResource(Res.string.cr_unlocked)
    "unavailable" -> stringResource(Res.string.cr_unavailable)
    "unknown" -> stringResource(Res.string.cr_unknown)
    "idle" -> stringResource(Res.string.cr_idle)
    "home" -> stringResource(Res.string.cr_home)
    "not_home", "away" -> stringResource(Res.string.cr_away)
    "cleaning" -> stringResource(Res.string.cr_cleaning)
    "returning" -> stringResource(Res.string.cr_returning)
    "paused" -> stringResource(Res.string.cr_paused)
    "error" -> stringResource(Res.string.cr_error)
    "playing" -> stringResource(Res.string.cr_playing)
    "buffering" -> stringResource(Res.string.cr_buffering)
    "standby" -> stringResource(Res.string.cr_standby)
    "humidifying" -> stringResource(Res.string.cr_humidifying)
    "drying" -> stringResource(Res.string.cr_drying)
    "detected" -> stringResource(Res.string.cr_detected)
    "clear" -> stringResource(Res.string.cr_clear)
    "wet" -> stringResource(Res.string.cr_wet)
    "dry" -> stringResource(Res.string.cr_dry)
    "active" -> stringResource(Res.string.ui_active_a733b80)
    "armed_home" -> stringResource(Res.string.ui_armed_home_c8d0b44)
    "armed_away" -> stringResource(Res.string.ui_armed_away_6834abf)
    "armed_night" -> stringResource(Res.string.ui_armed_night_80659bd)
    "armed_vacation", "armed_custom_bypass" -> stringResource(Res.string.ui_armed_32caa31)
    "disarmed" -> stringResource(Res.string.ui_disarmed_aaa4d9e)
    "triggered" -> stringResource(Res.string.ui_alarm_triggered_641ff6f)
    "pending" -> stringResource(Res.string.ui_alarm_pending_e2c04aa)
    else -> state.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

/** Localized presentation for Home Assistant HVAC mode tokens. */
@Composable
fun localizedHvacModeLabel(mode: String): String = when (mode.trim().lowercase()) {
    "auto" -> stringResource(Res.string.cr_mode_auto)
    "heat" -> stringResource(Res.string.cr_mode_heat)
    "cool" -> stringResource(Res.string.cr_mode_cool)
    "heat_cool" -> stringResource(Res.string.cr_mode_heat_cool)
    "dry" -> stringResource(Res.string.cr_mode_dry)
    "fan_only" -> stringResource(Res.string.cr_mode_fan_only)
    "off" -> stringResource(Res.string.cr_state_off)
    else -> mode.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
