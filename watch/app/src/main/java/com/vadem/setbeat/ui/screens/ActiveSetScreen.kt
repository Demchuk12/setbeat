package com.vadem.setbeat.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.vadem.setbeat.R
import com.vadem.setbeat.domain.WorkoutState
import com.vadem.setbeat.sensor.HeartRateReading
import com.vadem.setbeat.ui.components.CenteredText
import com.vadem.setbeat.ui.components.HeartRateLine
import com.vadem.setbeat.ui.components.ListScreen
import com.vadem.setbeat.ui.components.PrimaryAction
import com.vadem.setbeat.ui.formatDuration

@Composable
internal fun ActiveSetScreen(
    state: WorkoutState,
    heartRate: HeartRateReading,
    now: Long,
    onEnd: () -> Unit,
    onPause: () -> Unit,
) {
    val exercise = checkNotNull(state.currentExercise)
    val target = checkNotNull(state.target())
    ListScreen(action = PrimaryAction(stringResource(R.string.set_end), onEnd)) {
        item { CenteredText(exercise.name, MaterialTheme.typography.titleSmall) }
        item { CenteredText(setLabel(state.position, exercise) + " · " + targetLabel(target), MaterialTheme.typography.bodySmall) }
        item { CenteredText(formatDuration(state.phaseElapsedMillis(now)), MaterialTheme.typography.displaySmall, phaseColor(state, now)) }
        item { HeartRateLine(heartRate) }
        item { CompactButton(onClick = onPause, colors = ButtonDefaults.filledTonalButtonColors(), label = { Text(stringResource(R.string.pause)) }) }
    }
}
