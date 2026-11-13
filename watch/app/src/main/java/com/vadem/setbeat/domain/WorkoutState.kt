package com.vadem.setbeat.domain

enum class Phase { IDLE, SET_ACTIVE, RECOVERY, PAUSED, INTERRUPTED, COMPLETED }

data class Position(val exercise: Int, val set: Int)

data class SetTarget(val weight: Double, val reps: Int)

data class EndedSet(val position: Position, val startedAt: Long, val endedAt: Long)

data class SetResult(
    val position: Position,
    val weight: Double,
    val reps: Int,
    val rpe: Double?,
    val startedAt: Long,
    val endedAt: Long,
)

data class WorkoutState(
    val plan: WorkoutPlan,
    val startedAt: Long,
    val phase: Phase = Phase.IDLE,
    val phaseStartedAt: Long = startedAt,
    val position: Position = Position(0, 0),
    val setStartedAt: Long? = null,
    val unlogged: EndedSet? = null,
    val restMillis: Long = 0,
    val results: List<SetResult> = emptyList(),
    val pausedPhase: Phase? = null,
    val pausedAt: Long? = null,
    val endedAt: Long? = null,
) {
    val isFinished: Boolean get() = phase == Phase.COMPLETED || phase == Phase.INTERRUPTED

    val isPlanDone: Boolean get() = position.exercise >= plan.exercises.size

    val currentExercise: PlannedExercise? get() = plan.exercises.getOrNull(position.exercise)

    val totalSets: Int get() = plan.exercises.sumOf { it.sets }

    val volume: Double get() = results.sumOf { it.weight * it.reps }

    fun target(at: Position = position): SetTarget? {
        val exercise = plan.exercises.getOrNull(at.exercise) ?: return null
        val previous = results.lastOrNull { it.position.exercise == at.exercise }
        return SetTarget(previous?.weight ?: exercise.weight, previous?.reps ?: exercise.reps)
    }

    fun restRemainingMillis(now: Long): Long? =
        if (phase == Phase.RECOVERY) restMillis - (now - phaseStartedAt) else null

    fun phaseElapsedMillis(now: Long): Long = (pausedAt ?: now) - phaseStartedAt
}
