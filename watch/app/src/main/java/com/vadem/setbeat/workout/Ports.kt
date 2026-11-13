package com.vadem.setbeat.workout

import com.vadem.setbeat.domain.WorkoutEvent
import com.vadem.setbeat.domain.WorkoutPlan

data class OpenSession(val id: String, val events: List<WorkoutEvent>)

interface WorkoutEventStore {
    suspend fun createSession(id: String, startedAt: Long)

    suspend fun append(sessionId: String, event: WorkoutEvent)

    suspend fun findOpenSession(): OpenSession?

    suspend fun endSession(id: String, endedAt: Long)
}

fun interface PlanSource {
    suspend fun load(): WorkoutPlan
}

interface ExerciseTracker {
    fun start(sessionId: String)

    fun resume(sessionId: String)

    fun stop()
}

interface TrackingListener {
    suspend fun onTrackingEnded(reason: String)
}

interface RestTimer {
    fun schedule(remainingMillis: Long)

    fun cancel()
}

fun interface SessionArchive {
    suspend fun archive(sessionId: String)
}

fun interface Clock {
    fun now(): Long
}

interface Logger {
    fun info(message: String)

    fun warn(message: String, error: Throwable? = null)
}
