package com.vadem.setbeat.data.export

import com.vadem.setbeat.data.db.HeartRateSampleEntity
import com.vadem.setbeat.data.db.SessionEntity
import com.vadem.setbeat.data.db.WorkoutEventEntity

import com.vadem.setbeat.domain.WorkoutEvent
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionExportTest {

    private val session = SessionEntity(id = "s1", exerciseType = "STRENGTH_TRAINING", startedAt = 1_000, endedAt = 90_000)

    private fun event(seq: Int, event: WorkoutEvent) = WorkoutEventEntity(
        id = "e$seq",
        sessionId = "s1",
        seq = seq,
        type = event::class.simpleName!!,
        payload = WorkoutEvent.encode(event),
        timestamp = event.at,
        deviceId = "d1",
        schemaVersion = 1,
    )

    private val events = listOf(event(0, WorkoutEvent.SetStarted(10_000)), event(1, WorkoutEvent.SetEnded(40_000)))
    private val samples = listOf(
        HeartRateSampleEntity(id = 1, sessionId = "s1", bpm = 98.0, timestamp = 10_500, accuracy = "ACCURACY_HIGH"),
        HeartRateSampleEntity(id = 2, sessionId = "s1", bpm = 131.0, timestamp = 39_500, accuracy = null),
    )

    @Test
    fun `maps session with events and samples`() {
        val export = SessionExport.of(session, events, samples)

        assertEquals(SessionExport.SCHEMA_VERSION, export.schemaVersion)
        assertEquals(90_000L, export.endedAt)
        assertEquals(listOf("set_started", "set_ended"), export.events.map { it.payload["type"]?.jsonPrimitive?.content })
        assertEquals(listOf(98.0, 131.0), export.samples.map { it.bpm })
    }

    @Test
    fun `round trips through json`() {
        val export = SessionExport.of(session, events, samples)

        assertEquals(export, SessionExport.decode(export.encode()))
    }

    @Test
    fun `keeps missing accuracy and open session as null`() {
        val text = SessionExport.of(session.copy(endedAt = null), emptyList(), samples).encode()

        assertTrue(text.contains("\"endedAt\": null"))
        assertTrue(text.contains("\"accuracy\": null"))
    }
}
