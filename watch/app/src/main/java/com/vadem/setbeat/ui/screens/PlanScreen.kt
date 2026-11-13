package com.vadem.setbeat.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.MaterialTheme
import com.vadem.setbeat.R
import com.vadem.setbeat.ui.components.CenteredText
import com.vadem.setbeat.ui.components.ListScreen
import com.vadem.setbeat.ui.components.PrimaryAction
import com.vadem.setbeat.ui.formatWeight
import com.vadem.setbeat.workout.PlanStatus

@Composable
internal fun PlanScreen(status: PlanStatus, onStart: () -> Unit) {
    val plan = (status as? PlanStatus.Ready)?.plan
    ListScreen(action = plan?.let { PrimaryAction(stringResource(R.string.start_workout), onStart) }) {
        if (plan == null) {
            item {
                val message = if (status is PlanStatus.Failed) R.string.plan_error else R.string.plan_loading
                CenteredText(stringResource(message))
            }
            return@ListScreen
        }
        item { CenteredText(plan.name, MaterialTheme.typography.titleMedium) }
        plan.exercises.forEach { exercise ->
            item {
                CenteredText(
                    exercise.name + "\n" + stringResource(R.string.sets_line, exercise.sets, exercise.reps, formatWeight(exercise.weight)),
                    MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
