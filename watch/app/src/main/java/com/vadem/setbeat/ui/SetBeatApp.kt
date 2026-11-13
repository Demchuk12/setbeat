package com.vadem.setbeat.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.HealthPermissions
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.material3.AppScaffold
import com.vadem.setbeat.AppContainer
import com.vadem.setbeat.ui.screens.PermissionScreen
import com.vadem.setbeat.ui.screens.WorkoutScreen
import com.vadem.setbeat.ui.theme.SetBeatTheme
import kotlinx.coroutines.delay

private val heartRatePermission: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        HealthPermissions.READ_HEART_RATE
    } else {
        Manifest.permission.BODY_SENSORS
    }

private val requestedPermissions: Array<String>
    get() = arrayOf(heartRatePermission, Manifest.permission.POST_NOTIFICATIONS)

@Composable
fun SetBeatApp(container: AppContainer) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasHeartRatePermission()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        granted = it[heartRatePermission] ?: granted
    }
    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(requestedPermissions)
    }

    SetBeatTheme {
        AppScaffold {
            if (granted) {
                val viewModel: WorkoutViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { WorkoutViewModel(container.workoutController, container.heartRateMonitor) }
                    }
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                WorkoutScreen(state = state, now = rememberNow(), actions = viewModel)
            } else {
                PermissionScreen(onRequest = { launcher.launch(requestedPermissions) })
            }
        }
    }
}

@Composable
private fun rememberNow(): Long {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000 - value % 1_000)
        }
    }
    return now
}

private fun Context.hasHeartRatePermission() =
    ContextCompat.checkSelfPermission(this, heartRatePermission) == PackageManager.PERMISSION_GRANTED
