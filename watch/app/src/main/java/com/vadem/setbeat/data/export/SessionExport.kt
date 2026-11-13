package com.vadem.setbeat.data.export

import com.vadem.setbeat.data.db.HeartRateSampleEntity
import com.vadem.setbeat.data.db.SessionEntity
import com.vadem.setbeat.data.db.WorkoutEventEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Serializable
data class SessionExport(
    val schemaVersion: Int,
    val id: String,
    val exerciseType: String,
    val startedAt: Long,
    val endedAt: Long?,
    val events: List<Event>,
    val samples: List<Sample>,
) {
    @Serializable
    data class Event(
        val id: String,
        val seq: Int,
        val timestamp: Long,
        val deviceId: String,
        val schemaVersion: Int,
        val payload: JsonObject,
    )

    @Serializable
    data class Sample(val timestamp: Long, val bpm: Double, val accuracy: String?)

    companion object {
        const val SCHEMA_VERSION = 2

        private val json = Json { prettyPrint = true }

        fun of(session: SessionEntity, events: List<WorkoutEventEntity>, samples: List<HeartRateSampleEntity>) = SessionExport(
            schemaVersion = SCHEMA_VERSION,
            id = session.id,
            exerciseType = session.exerciseType,
            startedAt = session.startedAt,
            endedAt = session.endedAt,
            events = events.map {
                Event(it.id, it.seq, it.timestamp, it.deviceId, it.schemaVersion, json.parseToJsonElement(it.payload).jsonObject)
            },
            samples = samples.map { Sample(it.timestamp, it.bpm, it.accuracy) },
        )

        fun decode(text: String): SessionExport = json.decodeFromString(serializer(), text)
    }

    fun encode(): String = json.encodeToString(serializer(), this)
}
