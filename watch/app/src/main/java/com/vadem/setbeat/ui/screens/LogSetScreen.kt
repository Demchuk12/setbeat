package com.vadem.setbeat.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.MaterialTheme
import com.vadem.setbeat.R
import com.vadem.setbeat.domain.WorkoutState
import com.vadem.setbeat.sensor.HeartRateReading
import com.vadem.setbeat.ui.components.CenteredText
import com.vadem.setbeat.ui.components.HeartRateLine
import com.vadem.setbeat.ui.components.ListScreen
import com.vadem.setbeat.ui.components.PrimaryAction
import com.vadem.setbeat.ui.components.Stepper
import com.vadem.setbeat.ui.formatRpe
import com.vadem.setbeat.ui.formatWeight
import com.vadem.setbeat.ui.nextRpe
import com.vadem.setbeat.ui.previousRpe

@Composable
internal fun LogSetScreen(
    state: WorkoutState,
    heartRate: HeartRateReading,
    now: Long,
    onSave: (weight: Double, reps: Int, rpe: Double?) -> Unit,
) {
    val ended = checkNotNull(state.unlogged)
    val exercise = state.plan.exercises[ended.position.exercise]
    val target = checkNotNull(state.target(ended.position))
    var weight by remember(ended) { mutableDoubleStateOf(target.weight) }
    var reps by remember(ended) { mutableIntStateOf(target.reps) }
    var rpe by remember(ended) { mutableStateOf<Double?>(null) }

    ListScreen(action = PrimaryAction(stringResource(R.string.save), { onSave(weight, reps, rpe) })) {
        item { CenteredText(restLabel(state, now), MaterialTheme.typography.titleLarge, phaseColor(state, now)) }
        item { HeartRateLine(heartRate) }
        item { CenteredText(exercise.name + " · " + setLabel(ended.position, exercise), MaterialTheme.typography.bodySmall) }
        item {
            Stepper(
                value = formatWeight(weight),
                unit = stringResource(R.string.unit_kg),
                onDecrease = { weight = (weight - exercise.weightStep).coerceAtLeast(0.0) },
                onIncrease = { weight += exercise.weightStep },
                decreaseEnabled = weight > 0,
            )
        }
        item {
            Stepper(
                value = reps.toString(),
                unit = stringResource(R.string.unit_reps),
                onDecrease = { reps = (reps - 1).coerceAtLeast(0) },
                onIncrease = { reps += 1 },
                decreaseEnabled = reps > 0,
            )
        }
        item {
            Stepper(
                value = formatRpe(rpe),
                unit = stringResource(R.string.unit_rpe),
                onDecrease = { rpe = previousRpe(rpe) },
                onIncrease = { rpe = nextRpe(rpe) },
                decreaseEnabled = rpe != null,
            )
        }
    }
}
