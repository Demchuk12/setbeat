package com.vadem.setbeat.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatTest {
    @Test
    fun `formats weights without trailing zeros`() {
        assertEquals("80", formatWeight(80.0))
        assertEquals("0", formatWeight(0.0))
    }

    @Test
    fun `formats durations as minutes and seconds`() {
        assertEquals("2:05", formatDuration(125_000))
        assertEquals("-0:03", formatDuration(-3_000))
    }

    @Test
    fun `steps rpe between 6 and 10`() {
        assertEquals(6.0, nextRpe(null), 0.0)
        assertEquals(10.0, nextRpe(10.0), 0.0)
        assertEquals(7.5, previousRpe(8.0))
        assertNull(previousRpe(6.0))
        assertEquals("—", formatRpe(null))
    }
}
