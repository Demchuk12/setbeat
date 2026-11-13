package com.vadem.setbeat.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HeartRateReading(
    val bpm: Int? = null,
    val sensorStatus: String? = null,
    val storedSamples: Int = 0,
    val unavailable: Boolean = false,
)

class HeartRateMonitor {
    private val _reading = MutableStateFlow(HeartRateReading())
    val reading: StateFlow<HeartRateReading> = _reading.asStateFlow()

    fun update(transform: (HeartRateReading) -> HeartRateReading) = _reading.update(transform)

    fun reset() {
        _reading.value = HeartRateReading()
    }
}
