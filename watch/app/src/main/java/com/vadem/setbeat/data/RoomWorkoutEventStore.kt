package com.vadem.setbeat.data

import com.vadem.setbeat.data.db.SessionDao
import com.vadem.setbeat.data.db.SessionEntity
import com.vadem.setbeat.data.db.WorkoutEventDao
import com.vadem.setbeat.data.db.WorkoutEventEntity
import com.vadem.setbeat.domain.WorkoutEvent
import com.vadem.setbeat.workout.OpenSession
import com.vadem.setbeat.workout.WorkoutEventStore
import java.util.UUID

class RoomWorkoutEventStore(
    private val sessions: SessionDao,
    private val events: WorkoutEventDao,
    private val deviceId: () -> String,
) : WorkoutEventStore {

    override suspend fun createSession(id: String, startedAt: Long) {
        sessions.insert(SessionEntity(id = id, exerciseType = EXERCISE_TYPE, startedAt = startedAt))
    }

    override suspend fun append(sessionId: String, event: WorkoutEvent) {
        events.insert(
            WorkoutEventEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                seq = events.nextSeq(sessionId),
                type = event::class.simpleName.orEmpty(),
                payload = WorkoutEvent.encode(event),
                timestamp = event.at,
                deviceId = deviceId(),
                schemaVersion = EVENT_SCHEMA_VERSION,
            )
        )
    }

    override suspend fun findOpenSession(): OpenSession? {
        val open = sessions.findOpen() ?: return null
        return OpenSession(open.id, events.forSession(open.id).map { WorkoutEvent.decode(it.payload) })
    }

    override suspend fun endSession(id: String, endedAt: Long) = sessions.end(id, endedAt)

    private companion object {
        const val EXERCISE_TYPE = "STRENGTH_TRAINING"
        const val EVENT_SCHEMA_VERSION = 1
    }
}
