package com.vadem.setbeat.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.MaterialTheme
import com.vadem.setbeat.R
import com.vadem.setbeat.domain.Phase
import com.vadem.setbeat.domain.WorkoutState
import com.vadem.setbeat.ui.components.CenteredText
import com.vadem.setbeat.ui.components.ListScreen
import com.vadem.setbeat.ui.components.PrimaryAction
import com.vadem.setbeat.ui.formatDuration
import com.vadem.setbeat.ui.formatWeight

@Composable
internal fun SummaryScreen(state: WorkoutState, onDone: () -> Unit) {
    val title = if (state.phase == Phase.COMPLETED) R.string.completed else R.string.interrupted
    ListScreen(action = PrimaryAction(stringResource(R.string.done), onDone)) {
        item { CenteredText(stringResource(title), MaterialTheme.typography.titleMedium) }
        item { CenteredText(stringResource(R.string.summary_sets, state.results.size)) }
        item { CenteredText(stringResource(R.string.summary_volume, formatWeight(state.volume))) }
        item { CenteredText(stringResource(R.string.summary_duration, formatDuration(checkNotNull(state.endedAt) - state.startedAt))) }
    }
}
