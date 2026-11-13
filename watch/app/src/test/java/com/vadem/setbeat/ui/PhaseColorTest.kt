package com.vadem.setbeat.ui

import com.vadem.setbeat.domain.PlannedExercise
import com.vadem.setbeat.domain.WorkoutEngine
import com.vadem.setbeat.domain.WorkoutEvent
import com.vadem.setbeat.domain.WorkoutPlan
import com.vadem.setbeat.ui.screens.phaseColor
import com.vadem.setbeat.ui.theme.WorkoutColors
import org.junit.Assert.assertEquals
import org.junit.Test

class PhaseColorTest {
    private val plan = WorkoutPlan("p", "P", listOf(PlannedExercise("Squat", sets = 2, reps = 5, weight = 100.0, restSeconds = 60)))
    private val idle = WorkoutEngine.start(WorkoutEvent.SessionStarted(plan, 0))
    private val active = WorkoutEngine.reduce(idle, WorkoutEvent.SetStarted(1_000))
    private val resting = WorkoutEngine.reduce(active, WorkoutEvent.SetEnded(31_000))

    @Test
    fun `effort while lifting and when it is time to lift`() {
        assertEquals(WorkoutColors.effort, phaseColor(idle, 0))
        assertEquals(WorkoutColors.effort, phaseColor(active, 10_000))
        assertEquals(WorkoutColors.effort, phaseColor(resting, 91_000))
    }

    @Test
    fun `recovery while the rest timer runs`() {
        assertEquals(WorkoutColors.recovery, phaseColor(resting, 40_000))
    }
}
