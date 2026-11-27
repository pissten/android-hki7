package com.jimz011apps.hki7.data

import org.junit.Assert.assertEquals
import org.junit.Test

class Hki7EndpointAuthTest {
    private val now = 1_000_000L

    @Test
    fun `healthy access token is used directly`() {
        assertEquals(
            EndpointAuthDecision.USE_ACCESS_TOKEN,
            endpointAuthDecision("access", "refresh", now + 60_001L, now),
        )
    }

    @Test
    fun `expired or missing access token uses the refresh token`() {
        assertEquals(
            EndpointAuthDecision.REFRESH,
            endpointAuthDecision("access", "refresh", now, now),
        )
        assertEquals(
            EndpointAuthDecision.REFRESH,
            endpointAuthDecision(null, "refresh", null, now),
        )
    }

    @Test
    fun `expired access token without refresh requires login`() {
        assertEquals(
            EndpointAuthDecision.LOGIN_REQUIRED,
            endpointAuthDecision("access", null, now, now),
        )
    }

    @Test
    fun `preferred endpoint url wins over external and internal fallbacks`() {
        assertEquals(
            "http://10.0.0.53:8123",
            resolveEndpointUrl("http://10.0.0.53:8123", "https://example.ui.nabu.casa", "http://10.0.0.53:8123"),
        )
        assertEquals(
            "https://example.ui.nabu.casa",
            resolveEndpointUrl(null, "https://example.ui.nabu.casa", "http://10.0.0.53:8123"),
        )
        assertEquals(
            "http://10.0.0.53:8123",
            resolveEndpointUrl("   ", "", "http://10.0.0.53:8123"),
        )
    }
}
