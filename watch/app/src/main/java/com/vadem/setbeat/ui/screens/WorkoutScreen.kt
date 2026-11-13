package com.vadem.setbeat.ui.screens

import androidx.compose.runtime.Composable
import com.vadem.setbeat.domain.Phase
import com.vadem.setbeat.ui.WorkoutActions
import com.vadem.setbeat.ui.WorkoutUiState

@Composable
fun WorkoutScreen(state: WorkoutUiState, now: Long, actions: WorkoutActions) {
    val workout = state.workout
    when {
        workout == null -> PlanScreen(state.plan, actions::startSession)
        workout.isFinished -> SummaryScreen(workout, actions::dismiss)
        workout.phase == Phase.PAUSED -> PausedScreen(workout, now, actions::resume, actions::finish)
        workout.phase == Phase.SET_ACTIVE -> ActiveSetScreen(workout, state.heartRate, now, actions::endSet, actions::pause)
        workout.unlogged != null -> LogSetScreen(workout, state.heartRate, now, actions::logSet)
        else -> RestScreen(workout, state.heartRate, now, actions::startSet, actions::pause, actions::finish)
    }
}
