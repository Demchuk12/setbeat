package com.vadem.setbeat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.vadem.setbeat.R
import com.vadem.setbeat.domain.Phase
import com.vadem.setbeat.domain.WorkoutState
import com.vadem.setbeat.sensor.HeartRateReading
import com.vadem.setbeat.ui.components.CenteredText
import com.vadem.setbeat.ui.components.HeartRateLine
import com.vadem.setbeat.ui.components.ListScreen
import com.vadem.setbeat.ui.components.PrimaryAction

@Composable
internal fun RestScreen(
    state: WorkoutState,
    heartRate: HeartRateReading,
    now: Long,
    onStartSet: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
) {
    val exercise = state.currentExercise
    val target = state.target()
    val action = if (exercise == null) {
        PrimaryAction(stringResource(R.string.finish), onFinish)
    } else {
        PrimaryAction(stringResource(R.string.set_start), onStartSet)
    }
    ListScreen(action = action) {
        item {
            val title = if (state.phase == Phase.RECOVERY) restLabel(state, now) else stringResource(R.string.ready)
            CenteredText(title, MaterialTheme.typography.displayMedium, phaseColor(state, now))
        }
        item { HeartRateLine(heartRate) }
        if (exercise == null || target == null) {
            item { CenteredText(stringResource(R.string.plan_done), MaterialTheme.typography.titleMedium) }
            return@ListScreen
        }
        item {
            CenteredText(
                stringResource(R.string.next) + ": " + exercise.name + "\n" + setLabel(state.position, exercise) + " · " + targetLabel(target),
                MaterialTheme.typography.bodySmall,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CompactButton(onClick = onPause, colors = ButtonDefaults.filledTonalButtonColors(), label = { Text(stringResource(R.string.pause)) })
                CompactButton(onClick = onFinish, colors = ButtonDefaults.filledTonalButtonColors(), label = { Text(stringResource(R.string.finish)) })
            }
        }
    }
}
