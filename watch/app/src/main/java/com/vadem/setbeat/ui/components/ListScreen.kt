package com.vadem.setbeat.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text

data class PrimaryAction(
    val label: String,
    val onClick: () -> Unit,
    val size: EdgeButtonSize = EdgeButtonSize.Medium,
)

@Composable
fun ListScreen(action: PrimaryAction? = null, content: ScalingLazyListScope.() -> Unit) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    val list: @Composable BoxScope.(PaddingValues) -> Unit = { contentPadding ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = contentPadding,
            autoCentering = null,
            content = content,
        )
    }
    if (action == null) {
        ScreenScaffold(scrollState = listState, content = list)
    } else {
        ScreenScaffold(
            scrollState = listState,
            edgeButton = { EdgeButton(onClick = action.onClick, buttonSize = action.size) { Text(action.label) } },
            content = list,
        )
    }
}
