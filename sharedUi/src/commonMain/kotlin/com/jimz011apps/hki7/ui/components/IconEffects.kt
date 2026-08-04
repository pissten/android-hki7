package com.jimz011apps.hki7.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.jimz011apps.hki7.data.HAEntity
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

enum class IconEffect { NONE, GLOW, SPIN, PULSE }

val LocalIconAnimationsEnabled = compositionLocalOf { false }

private val INACTIVE = setOf("unavailable", "unknown", "none", "")

fun isEntityActive(entity: HAEntity): Boolean {
    val s = entity.state.trim().lowercase()
    if (s in INACTIVE) return false
    return when (entity.entity_id.substringBefore('.')) {
        "light", "switch", "input_boolean", "fan", "siren", "humidifier" -> s == "on"
        "media_player" -> s == "playing" || s == "on" || s == "buffering"
        "vacuum" -> s == "cleaning" || s == "returning"
        "climate", "water_heater" -> s != "off"
        "lock" -> s == "unlocked"
        "cover", "valve" -> s == "opening" || s == "closing"
        "binary_sensor" -> s == "on"
        "automation", "script" -> s == "on"
        "alarm_control_panel" -> s.startsWith("armed") || s == "triggered"
        "person", "device_tracker", "sensor", "weather", "sun" -> false
        else -> s == "on"
    }
}

val IconEffectGroups: List<Pair<String, String>> = listOf(
    "lights" to "Lights & switches",
    "fans" to "Fans & vacuums",
    "media" to "Media players",
    "climate" to "Climate & humidity",
    "alerts" to "Sensors & alarms",
    "other" to "Everything else",
)

val DefaultIconEffectByGroup: Map<String, String> = mapOf(
    "lights" to "glow",
    "fans" to "spin",
    "media" to "pulse",
    "climate" to "pulse",
    "alerts" to "pulse",
    "other" to "pulse",
)

object IconEffectDefaults {
    var byGroup: Map<String, String> = emptyMap()
}

private fun groupForDomain(domain: String): String = when (domain) {
    "light", "switch", "input_boolean" -> "lights"
    "fan", "vacuum" -> "fans"
    "media_player" -> "media"
    "climate", "humidifier", "water_heater" -> "climate"
    "binary_sensor", "siren", "alarm_control_panel" -> "alerts"
    else -> "other"
}

private fun effectFromId(id: String): IconEffect = when (id) {
    "glow" -> IconEffect.GLOW
    "spin" -> IconEffect.SPIN
    "pulse" -> IconEffect.PULSE
    else -> IconEffect.NONE
}

private fun domainEffect(entity: HAEntity): IconEffect {
    val group = groupForDomain(entity.entity_id.substringBefore('.'))
    val id = IconEffectDefaults.byGroup[group] ?: DefaultIconEffectByGroup.getValue(group)
    return effectFromId(id)
}

fun iconEffectFor(entity: HAEntity, enabled: Boolean, override: String = "auto"): IconEffect {
    if (override == "off") return IconEffect.NONE
    if (!isEntityActive(entity)) return IconEffect.NONE
    return when (override) {
        "glow" -> IconEffect.GLOW
        "spin" -> IconEffect.SPIN
        "pulse" -> IconEffect.PULSE
        else -> if (enabled) domainEffect(entity) else IconEffect.NONE
    }
}

private val NON_SPINNING_ICON_SLUGS = setOf("air-purifier", "air-humidifier", "air-conditioner")

fun IconEffect.forIconSlug(iconSlug: String?): IconEffect =
    if (this == IconEffect.SPIN && iconSlug in NON_SPINNING_ICON_SLUGS) IconEffect.PULSE else this

private fun spinPeriodMillis(entity: HAEntity?): Int {
    return when (entity?.entity_id?.substringBefore('.')) {
        "fan" -> {
            val pct = entity?.attributes?.get("percentage")?.jsonPrimitive?.intOrNull?.coerceIn(0, 100)
            if (pct != null) (2400 - (pct / 100f) * 1700).toInt() else 1400
        }
        "vacuum" -> 1600
        else -> 1500
    }
}

@Composable
fun WithIconEffect(
    entity: HAEntity?,
    effect: IconEffect,
    glowColor: Color,
    content: @Composable (Modifier) -> Unit,
) {
    when (effect) {
        IconEffect.NONE -> content(Modifier)

        IconEffect.SPIN -> {
            val t = rememberInfiniteTransition(label = "spin")
            val rotation by t.animateFloat(
                0f, 360f,
                infiniteRepeatable(tween(spinPeriodMillis(entity), easing = LinearEasing), RepeatMode.Restart),
                label = "rotation",
            )
            content(Modifier.graphicsLayer { rotationZ = rotation })
        }

        IconEffect.PULSE -> {
            val t = rememberInfiniteTransition(label = "pulse")
            val scale by t.animateFloat(
                1f, 1f,
                infiniteRepeatable(
                    keyframes {
                        durationMillis = 1600
                        1f at 0
                        1.16f at 130 using FastOutSlowInEasing
                        1f at 320 using FastOutSlowInEasing
                        1.11f at 460 using FastOutSlowInEasing
                        1f at 640 using FastOutSlowInEasing
                        1f at 1600
                    },
                    RepeatMode.Restart,
                ),
                label = "pulseScale",
            )
            content(Modifier.graphicsLayer { scaleX = scale; scaleY = scale })
        }

        IconEffect.GLOW -> {
            val t = rememberInfiniteTransition(label = "glow")
            val phase by t.animateFloat(
                0f, 1f,
                infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "glowPhase",
            )
            val glowAlpha = 0.12f + 0.5f * phase
            val scale = 1f + 0.06f * phase
            content(
                Modifier
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = glowAlpha),
                                    glowColor.copy(alpha = glowAlpha * 0.35f),
                                    Color.Transparent,
                                ),
                                center = center,
                                radius = size.maxDimension * (0.85f + 0.35f * phase),
                            ),
                        )
                    }
                    .graphicsLayer { scaleX = scale; scaleY = scale },
            )
        }
    }
}
