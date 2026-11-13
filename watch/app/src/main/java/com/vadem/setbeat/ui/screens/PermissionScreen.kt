package com.vadem.setbeat.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vadem.setbeat.R
import com.vadem.setbeat.ui.components.CenteredText
import com.vadem.setbeat.ui.components.ListScreen
import com.vadem.setbeat.ui.components.PrimaryAction

@Composable
fun PermissionScreen(onRequest: () -> Unit) {
    ListScreen(action = PrimaryAction(stringResource(R.string.grant), onRequest)) {
        item { CenteredText(stringResource(R.string.permission_needed)) }
    }
}
