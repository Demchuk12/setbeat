package com.vadem.setbeat.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed interface WorkoutEvent {
    val at: Long

    @Serializable
    @SerialName("session_started")
    data class SessionStarted(val plan: WorkoutPlan, override val at: Long) : WorkoutEvent

    @Serializable
    @SerialName("set_started")
    data class SetStarted(override val at: Long) : WorkoutEvent

    @Serializable
    @SerialName("set_ended")
    data class SetEnded(override val at: Long) : WorkoutEvent

    @Serializable
    @SerialName("set_logged")
    data class SetLogged(val weight: Double, val reps: Int, val rpe: Double? = null, override val at: Long) : WorkoutEvent

    @Serializable
    @SerialName("paused")
    data class Paused(override val at: Long) : WorkoutEvent

    @Serializable
    @SerialName("resumed")
    data class Resumed(override val at: Long) : WorkoutEvent

    @Serializable
    @SerialName("finished")
    data class Finished(override val at: Long) : WorkoutEvent

    @Serializable
    @SerialName("interrupted")
    data class Interrupted(val reason: String, override val at: Long) : WorkoutEvent

    companion object {
        val json = Json {
            classDiscriminator = "type"
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun encode(event: WorkoutEvent): String = json.encodeToString(serializer(), event)

        fun decode(text: String): WorkoutEvent = json.decodeFromString(serializer(), text)
    }
}
