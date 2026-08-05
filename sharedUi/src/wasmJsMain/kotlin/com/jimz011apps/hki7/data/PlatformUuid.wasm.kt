package com.jimz011apps.hki7.data

import kotlin.random.Random

internal actual fun hki7RandomUuid(): String {
    val bytes = ByteArray(16)
    Random.nextBytes(bytes)
    bytes[6] = ((bytes[6].toInt() and 0x0F) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3F) or 0x80).toByte()
    val hex = bytes.joinToString("") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
    }
    return buildString(36) {
        append(hex, 0, 8)
        append('-')
        append(hex, 8, 12)
        append('-')
        append(hex, 12, 16)
        append('-')
        append(hex, 16, 20)
        append('-')
        append(hex, 20, 32)
    }
}
