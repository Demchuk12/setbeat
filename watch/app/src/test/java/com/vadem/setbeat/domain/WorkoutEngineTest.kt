package com.vadem.setbeat.domain

import com.vadem.setbeat.domain.WorkoutEvent.Finished
import com.vadem.setbeat.domain.WorkoutEvent.Interrupted
import com.vadem.setbeat.domain.WorkoutEvent.Paused
import com.vadem.setbeat.domain.WorkoutEvent.Resumed
import com.vadem.setbeat.domain.WorkoutEvent.SessionStarted
import com.vadem.setbeat.domain.WorkoutEvent.SetEnded
import com.vadem.setbeat.domain.WorkoutEvent.SetLogged
import com.vadem.setbeat.domain.WorkoutEvent.SetStarted
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutEngineTest {

    private val plan = WorkoutPlan(
        id = "test",
        name = "Test",
        exercises = listOf(
            PlannedExercise(name = "Squat", sets = 2, reps = 5, weight = 100.0, restSeconds = 120),
            PlannedExercise(name = "Row", sets = 1, reps = 10, weight = 50.0, restSeconds = 60),
        ),
    )

    private fun started() = WorkoutEngine.start(SessionStarted(plan, at = 0))

    private fun WorkoutState.apply(vararg events: WorkoutEvent) = events.fold(this, WorkoutEngine::reduce)

    @Test
    fun `new session waits for the first set`() {
        val state = started()

        assertEquals(Phase.IDLE, state.phase)
        assertEquals(Position(0, 0), state.position)
        assertEquals(SetTarget(100.0, 5), state.target())
    }

    @Test
    fun `ending a set starts recovery with the exercise rest`() {
        val state = started().apply(SetStarted(10_000), SetEnded(40_000))

        assertEquals(Phase.RECOVERY, state.phase)
        assertEquals(EndedSet(Position(0, 0), 10_000, 40_000), state.unlogged)
        assertEquals(120_000L, state.restRemainingMillis(now = 40_000))
        assertEquals(20_000L, state.restRemainingMillis(now = 140_000))
    }

    @Test
    fun `logging a set records the result and moves to the next set`() {
        val state = started().apply(SetStarted(10_000), SetEnded(40_000), SetLogged(102.5, 4, rpe = 8.5, at = 50_000))

        assertEquals(Phase.RECOVERY, state.phase)
        assertNull(state.unlogged)
        assertEquals(Position(0, 1), state.position)
        assertEquals(SetResult(Position(0, 0), 102.5, 4, 8.5, 10_000, 40_000), state.results.single())
    }

    @Test
    fun `next set target follows what was actually lifted`() {
        val state = started().apply(SetStarted(1), SetEnded(2), SetLogged(102.5, 4, at = 3))

        assertEquals(SetTarget(102.5, 4), state.target())
    }

    @Test
    fun `starting the next set without logging keeps the target values`() {
        val state = started().apply(SetStarted(1_000), SetEnded(2_000), SetStarted(90_000))

        assertEquals(Phase.SET_ACTIVE, state.phase)
        assertEquals(Position(0, 1), state.position)
        assertEquals(SetResult(Position(0, 0), 100.0, 5, null, 1_000, 2_000), state.results.single())
    }

    @Test
    fun `last set of an exercise moves to the next exercise`() {
        val state = started().apply(
            SetStarted(1), SetEnded(2), SetLogged(100.0, 5, at = 3),
            SetStarted(4), SetEnded(5), SetLogged(100.0, 5, at = 6),
        )

        assertEquals(Position(1, 0), state.position)
        assertEquals("Row", state.currentExercise?.name)
        assertEquals(120_000L, state.restMillis)

        val afterRow = state.apply(SetStarted(7), SetEnded(8))
        assertEquals(60_000L, afterRow.restMillis)
    }

    @Test
    fun `plan is done after the last set and no more sets can start`() {
        val state = started().apply(
            SetStarted(1), SetEnded(2), SetLogged(100.0, 5, at = 3),
            SetStarted(4), SetEnded(5), SetLogged(100.0, 5, at = 6),
            SetStarted(7), SetEnded(8), SetLogged(50.0, 10, at = 9),
        )

        assertTrue(state.isPlanDone)
        assertThrows(IllegalTransition::class.java) { WorkoutEngine.reduce(state, SetStarted(10)) }
        assertEquals(Phase.COMPLETED, WorkoutEngine.reduce(state, Finished(10)).phase)
    }

    @Test
    fun `pause freezes the rest timer and resume continues it`() {
        val paused = started().apply(SetStarted(0), SetEnded(10_000), Paused(40_000))

        assertEquals(Phase.PAUSED, paused.phase)
        assertEquals(30_000L, paused.phaseElapsedMillis(now = 500_000))

        val resumed = WorkoutEngine.reduce(paused, Resumed(100_000))

        assertEquals(Phase.RECOVERY, resumed.phase)
        assertEquals(90_000L, resumed.restRemainingMillis(now = 100_000))
    }

    @Test
    fun `pause during a set returns to the same set`() {
        val state = started().apply(SetStarted(0), Paused(5), Resumed(15))

        assertEquals(Phase.SET_ACTIVE, state.phase)
        assertEquals(0L, state.setStartedAt)
        assertEquals(Position(0, 0), state.position)
    }

    @Test
    fun `finish logs a pending set with target values`() {
        val state = started().apply(SetStarted(1), SetEnded(2), Finished(3))

        assertEquals(Phase.COMPLETED, state.phase)
        assertEquals(3L, state.endedAt)
        assertEquals(1, state.results.size)
    }

    @Test
    fun `cannot finish in the middle of a set`() {
        val active = started().apply(SetStarted(1))

        assertThrows(IllegalTransition::class.java) { WorkoutEngine.reduce(active, Finished(2)) }
        assertThrows(IllegalTransition::class.java) { WorkoutEngine.reduce(active.apply(Paused(2)), Finished(3)) }
    }

    @Test
    fun `interruption drops a running set but keeps logged ones`() {
        val state = started().apply(SetStarted(1), SetEnded(2), SetLogged(100.0, 5, at = 3), SetStarted(4), Interrupted("sensor lost", 5))

        assertEquals(Phase.INTERRUPTED, state.phase)
        assertEquals(1, state.results.size)
        assertNull(state.setStartedAt)
    }

    @Test
    fun `finished session accepts no more events`() {
        val state = started().apply(Finished(1))

        listOf(SetStarted(2), Paused(2), Finished(2), Interrupted("x", 2)).forEach { event ->
            assertFalse(WorkoutEngine.canApply(state, event))
        }
    }

    @Test
    fun `invalid transitions are rejected`() {
        val idle = started()

        assertThrows(IllegalTransition::class.java) { WorkoutEngine.reduce(idle, SetEnded(1)) }
        assertThrows(IllegalTransition::class.java) { WorkoutEngine.reduce(idle, SetLogged(1.0, 1, at = 1)) }
        assertThrows(IllegalTransition::class.java) { WorkoutEngine.reduce(idle, Resumed(1)) }
        assertThrows(IllegalTransition::class.java) { WorkoutEngine.reduce(idle.apply(SetStarted(1)), SetStarted(2)) }
        assertThrows(IllegalTransition::class.java) {
            WorkoutEngine.reduce(idle.apply(SetStarted(1), SetEnded(2), SetLogged(1.0, 1, at = 3)), SetLogged(1.0, 1, at = 4))
        }
    }

    @Test
    fun `replay rebuilds the same state as live updates`() {
        val events = listOf(
            SessionStarted(plan, 0),
            SetStarted(1_000), SetEnded(30_000), SetLogged(100.0, 5, 8.0, 35_000),
            Paused(60_000), Resumed(90_000),
            SetStarted(160_000), SetEnded(190_000),
        )

        val live = events.drop(1).fold(WorkoutEngine.start(events.first() as SessionStarted), WorkoutEngine::reduce)

        assertEquals(live, WorkoutEngine.replay(events))
    }

    @Test
    fun `replay survives a json round trip`() {
        val events = listOf(SessionStarted(plan, 0), SetStarted(1), SetEnded(2), SetLogged(100.0, 5, null, 3), Interrupted("app killed", 4))

        val decoded = events.map { WorkoutEvent.decode(WorkoutEvent.encode(it)) }

        assertEquals(events, decoded)
        assertEquals(WorkoutEngine.replay(events), WorkoutEngine.replay(decoded))
    }

    @Test
    fun `replay needs a session start`() {
        assertNull(WorkoutEngine.replay(emptyList()))
        assertNull(WorkoutEngine.replay(listOf(SetStarted(1))))
    }

    @Test
    fun `plan rejects invalid exercises`() {
        assertThrows(IllegalArgumentException::class.java) { PlannedExercise("x", sets = 0, reps = 5, weight = 10.0, restSeconds = 60) }
        assertThrows(IllegalArgumentException::class.java) { WorkoutPlan("x", "x", emptyList()) }
    }
}
