package com.vadem.setbeat.domain

data class HeartRateSample(
    val timestamp: Long,
    val bpm: Double,
    val accuracy: String?,
)
