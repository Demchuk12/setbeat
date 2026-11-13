package com.vadem.setbeat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadem.setbeat.domain.WorkoutState
import com.vadem.setbeat.sensor.HeartRateMonitor
import com.vadem.setbeat.sensor.HeartRateReading
import com.vadem.setbeat.workout.PlanStatus
import com.vadem.setbeat.workout.WorkoutController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class WorkoutUiState(
    val plan: PlanStatus = PlanStatus.Loading,
    val workout: WorkoutState? = null,
    val heartRate: HeartRateReading = HeartRateReading(),
)

interface WorkoutActions {
    fun startSession()
    fun startSet()
    fun endSet()
    fun logSet(weight: Double, reps: Int, rpe: Double?)
    fun pause()
    fun resume()
    fun finish()
    fun dismiss()
}

class WorkoutViewModel(
    private val controller: WorkoutController,
    heartRate: HeartRateMonitor,
) : ViewModel(), WorkoutActions {

    val uiState: StateFlow<WorkoutUiState> =
        combine(controller.plan, controller.state, heartRate.reading, ::WorkoutUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutUiState())

    override fun startSession() = controller.startSession()
    override fun startSet() = controller.startSet()
    override fun endSet() = controller.endSet()
    override fun logSet(weight: Double, reps: Int, rpe: Double?) = controller.logSet(weight, reps, rpe)
    override fun pause() = controller.pause()
    override fun resume() = controller.resume()
    override fun finish() = controller.finish()
    override fun dismiss() = controller.dismiss()
}
