package com.vadem.setbeat.domain

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutPlan(
    val id: String,
    val name: String,
    val exercises: List<PlannedExercise>,
) {
    init {
        require(exercises.isNotEmpty()) { "plan $id has no exercises" }
    }
}

@Serializable
data class PlannedExercise(
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val weightStep: Double = 2.5,
    val restSeconds: Int,
) {
    init {
        require(sets > 0 && reps > 0 && restSeconds >= 0 && weight >= 0 && weightStep > 0) {
            "invalid exercise $name"
        }
    }
}
