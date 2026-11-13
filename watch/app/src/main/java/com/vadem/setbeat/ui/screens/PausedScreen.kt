package com.vadem.setbeat.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.Text
import com.vadem.setbeat.R
import com.vadem.setbeat.domain.Phase
import com.vadem.setbeat.domain.WorkoutState
import com.vadem.setbeat.ui.components.CenteredText
import com.vadem.setbeat.ui.components.ListScreen
import com.vadem.setbeat.ui.components.PrimaryAction
import com.vadem.setbeat.ui.formatDuration

@Composable
internal fun PausedScreen(state: WorkoutState, now: Long, onResume: () -> Unit, onFinish: () -> Unit) {
    ListScreen(action = PrimaryAction(stringResource(R.string.resume), onResume, EdgeButtonSize.Large)) {
        item { CenteredText(stringResource(R.string.paused), MaterialTheme.typography.titleLarge) }
        item { CenteredText(formatDuration(now - checkNotNull(state.pausedAt)), MaterialTheme.typography.displayMedium) }
        if (state.pausedPhase != Phase.SET_ACTIVE) {
            item {
                OutlinedButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.finish)) }
            }
        }
    }
}
