package com.vadem.setbeat.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.vadem.setbeat.R
import com.vadem.setbeat.domain.Phase
import com.vadem.setbeat.domain.PlannedExercise
import com.vadem.setbeat.domain.Position
import com.vadem.setbeat.domain.SetTarget
import com.vadem.setbeat.domain.WorkoutState
import com.vadem.setbeat.ui.formatDuration
import com.vadem.setbeat.ui.formatWeight
import com.vadem.setbeat.ui.theme.WorkoutColors

@Composable
internal fun setLabel(position: Position, exercise: PlannedExercise) =
    stringResource(R.string.set_of, position.set + 1, exercise.sets)

@Composable
internal fun targetLabel(target: SetTarget) = stringResource(R.string.target, formatWeight(target.weight), target.reps)

@Composable
internal fun restLabel(state: WorkoutState, now: Long): String {
    val remaining = state.restRemainingMillis(now) ?: 0
    return if (remaining > 0) formatDuration(remaining) else stringResource(R.string.rest_over)
}

internal fun phaseColor(state: WorkoutState, now: Long): Color {
    val resting = state.phase == Phase.RECOVERY && (state.restRemainingMillis(now) ?: 0) > 0
    return if (resting) WorkoutColors.recovery else WorkoutColors.effort
}
