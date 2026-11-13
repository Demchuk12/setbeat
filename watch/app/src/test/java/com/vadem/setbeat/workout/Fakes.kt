package com.vadem.setbeat.workout

import com.vadem.setbeat.domain.PlannedExercise
import com.vadem.setbeat.domain.WorkoutEvent
import com.vadem.setbeat.domain.WorkoutPlan

val testPlan = WorkoutPlan(
    id = "test",
    name = "Test",
    exercises = listOf(PlannedExercise(name = "Squat", sets = 2, reps = 5, weight = 100.0, restSeconds = 120)),
)

class FakeStore : WorkoutEventStore {
    val events = linkedMapOf<String, MutableList<WorkoutEvent>>()
    val ended = mutableMapOf<String, Long>()

    override suspend fun createSession(id: String, startedAt: Long) {
        events[id] = mutableListOf()
    }

    override suspend fun append(sessionId: String, event: WorkoutEvent) {
        checkNotNull(events[sessionId]).add(event)
    }

    override suspend fun findOpenSession(): OpenSession? =
        events.entries.lastOrNull { it.key !in ended }?.let { OpenSession(it.key, it.value.toList()) }

    override suspend fun endSession(id: String, endedAt: Long) {
        ended[id] = endedAt
    }
}

class FakeTracker : ExerciseTracker {
    val calls = mutableListOf<String>()

    override fun start(sessionId: String) {
        calls += "start:$sessionId"
    }

    override fun resume(sessionId: String) {
        calls += "resume:$sessionId"
    }

    override fun stop() {
        calls += "stop"
    }
}

class FakeRestTimer : RestTimer {
    var scheduled: Long? = null

    override fun schedule(remainingMillis: Long) {
        scheduled = remainingMillis
    }

    override fun cancel() {
        scheduled = null
    }
}

class FakeArchive : SessionArchive {
    val archived = mutableListOf<String>()

    override suspend fun archive(sessionId: String) {
        archived += sessionId
    }
}

class FakeClock(var time: Long = 1_000_000) : Clock {
    override fun now() = time
}

object SilentLogger : Logger {
    override fun info(message: String) = Unit

    override fun warn(message: String, error: Throwable?) = Unit
}
