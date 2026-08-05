package com.jimz011apps.hki7.ui

import com.jimz011apps.hki7.data.HAEntity

/**
 * Platform-neutral primary Home Assistant action for an entity.
 *
 * The mapping is intentionally conservative: only domains with an unambiguous reversible or
 * explicit primary service are exposed. Android and web can render the same action while their
 * hosts send it through the shared Home Assistant session/client.
 */
data class Hki7EntityPrimaryAction(
    val domain: String,
    val service: String,
    val label: String,
)

fun HAEntity.primaryAction(): Hki7EntityPrimaryAction? {
    val domain = entity_id.substringBefore('.')
    val normalizedState = state.trim().lowercase()

    return when (domain) {
        "light", "switch", "input_boolean", "fan", "humidifier", "automation" ->
            Hki7EntityPrimaryAction(domain = domain, service = "toggle", label = "Toggle")

        "lock" -> if (normalizedState == "locked") {
            Hki7EntityPrimaryAction(domain = domain, service = "unlock", label = "Unlock")
        } else {
            Hki7EntityPrimaryAction(domain = domain, service = "lock", label = "Lock")
        }

        "cover" -> if (normalizedState == "closed" || normalizedState == "closing") {
            Hki7EntityPrimaryAction(domain = domain, service = "open_cover", label = "Open")
        } else {
            Hki7EntityPrimaryAction(domain = domain, service = "close_cover", label = "Close")
        }

        "media_player" -> if (normalizedState == "playing") {
            Hki7EntityPrimaryAction(domain = domain, service = "media_pause", label = "Pause")
        } else {
            Hki7EntityPrimaryAction(domain = domain, service = "media_play", label = "Play")
        }

        "climate" -> if (normalizedState == "off") {
            Hki7EntityPrimaryAction(domain = domain, service = "turn_on", label = "Turn on")
        } else {
            Hki7EntityPrimaryAction(domain = domain, service = "turn_off", label = "Turn off")
        }

        "vacuum" -> if (normalizedState in setOf("cleaning", "returning")) {
            Hki7EntityPrimaryAction(domain = domain, service = "stop", label = "Stop")
        } else {
            Hki7EntityPrimaryAction(domain = domain, service = "start", label = "Start")
        }

        "button", "input_button" ->
            Hki7EntityPrimaryAction(domain = domain, service = "press", label = "Press")

        else -> null
    }
}
