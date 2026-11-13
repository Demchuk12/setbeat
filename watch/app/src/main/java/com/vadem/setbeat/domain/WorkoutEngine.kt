package com.vadem.setbeat.domain

import com.vadem.setbeat.domain.WorkoutEvent.Finished
import com.vadem.setbeat.domain.WorkoutEvent.Interrupted
import com.vadem.setbeat.domain.WorkoutEvent.Paused
import com.vadem.setbeat.domain.WorkoutEvent.Resumed
import com.vadem.setbeat.domain.WorkoutEvent.SessionStarted
import com.vadem.setbeat.domain.WorkoutEvent.SetEnded
import com.vadem.setbeat.domain.WorkoutEvent.SetLogged
import com.vadem.setbeat.domain.WorkoutEvent.SetStarted

class IllegalTransition(phase: Phase, event: WorkoutEvent) :
    IllegalStateException("${event::class.simpleName} is not allowed in $phase")

object WorkoutEngine {

    fun start(event: SessionStarted): WorkoutState = WorkoutState(plan = event.plan, startedAt = event.at)

    fun replay(events: List<WorkoutEvent>): WorkoutState? {
        val first = events.firstOrNull() as? SessionStarted ?: return null
        return events.drop(1).fold(start(first), ::reduce)
    }

    fun canApply(state: WorkoutState, event: WorkoutEvent): Boolean =
        runCatching { reduce(state, event) }.isSuccess

    fun reduce(state: WorkoutState, event: WorkoutEvent): WorkoutState = when (event) {
        is SessionStarted -> throw IllegalTransition(state.phase, event)
        is SetStarted -> startSet(state, event)
        is SetEnded -> endSet(state, event)
        is SetLogged -> logSet(state, event)
        is Paused -> pause(state, event)
        is Resumed -> resume(state, event)
        is Finished -> finish(state, event, Phase.COMPLETED)
        is Interrupted -> finish(state, event, Phase.INTERRUPTED)
    }

    private fun startSet(state: WorkoutState, event: SetStarted): WorkoutState {
        state.expect(event, Phase.IDLE, Phase.RECOVERY)
        val logged = state.logDefaultsIfNeeded(event.at)
        if (logged.isPlanDone) throw IllegalTransition(state.phase, event)
        return logged.copy(phase = Phase.SET_ACTIVE, phaseStartedAt = event.at, setStartedAt = event.at)
    }

    private fun endSet(state: WorkoutState, event: SetEnded): WorkoutState {
        state.expect(event, Phase.SET_ACTIVE)
        val exercise = checkNotNull(state.currentExercise)
        return state.copy(
            phase = Phase.RECOVERY,
            phaseStartedAt = event.at,
            setStartedAt = null,
            unlogged = EndedSet(state.position, checkNotNull(state.setStartedAt), event.at),
            restMillis = exercise.restSeconds * 1000L,
        )
    }

    private fun logSet(state: WorkoutState, event: SetLogged): WorkoutState {
        state.expect(event, Phase.RECOVERY)
        if (state.unlogged == null) throw IllegalTransition(state.phase, event)
        return state.record(event.weight, event.reps, event.rpe)
    }

    private fun pause(state: WorkoutState, event: Paused): WorkoutState {
        state.expect(event, Phase.IDLE, Phase.SET_ACTIVE, Phase.RECOVERY)
        return state.copy(phase = Phase.PAUSED, pausedPhase = state.phase, pausedAt = event.at)
    }

    private fun resume(state: WorkoutState, event: Resumed): WorkoutState {
        state.expect(event, Phase.PAUSED)
        val pausedFor = event.at - checkNotNull(state.pausedAt)
        return state.copy(
            phase = checkNotNull(state.pausedPhase),
            phaseStartedAt = state.phaseStartedAt + pausedFor,
            pausedPhase = null,
            pausedAt = null,
        )
    }

    private fun finish(state: WorkoutState, event: WorkoutEvent, phase: Phase): WorkoutState {
        if (state.isFinished) throw IllegalTransition(state.phase, event)
        if (phase == Phase.COMPLETED && (state.phase == Phase.SET_ACTIVE || state.pausedPhase == Phase.SET_ACTIVE)) {
            throw IllegalTransition(state.phase, event)
        }
        return state.logDefaultsIfNeeded(event.at).copy(
            phase = phase,
            phaseStartedAt = event.at,
            setStartedAt = null,
            pausedPhase = null,
            pausedAt = null,
            endedAt = event.at,
        )
    }

    private fun WorkoutState.expect(event: WorkoutEvent, vararg phases: Phase) {
        if (phase !in phases) throw IllegalTransition(phase, event)
    }

    private fun WorkoutState.logDefaultsIfNeeded(at: Long): WorkoutState {
        val ended = unlogged ?: return this
        val target = checkNotNull(target(ended.position))
        return record(target.weight, target.reps, rpe = null)
    }

    private fun WorkoutState.record(weight: Double, reps: Int, rpe: Double?): WorkoutState {
        val ended = checkNotNull(unlogged)
        val result = SetResult(ended.position, weight, reps, rpe, ended.startedAt, ended.endedAt)
        return copy(results = results + result, unlogged = null, position = next(ended.position))
    }

    private fun WorkoutState.next(from: Position): Position {
        val exercise = plan.exercises[from.exercise]
        return if (from.set + 1 < exercise.sets) from.copy(set = from.set + 1) else Position(from.exercise + 1, 0)
    }
}
