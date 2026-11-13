package com.vadem.setbeat.ui

import kotlin.math.abs

private const val RPE_MIN = 6.0
private const val RPE_MAX = 10.0
private const val RPE_STEP = 0.5

fun formatWeight(weight: Double): String =
    if (weight % 1.0 == 0.0) weight.toLong().toString() else "%.1f".format(weight)

fun formatDuration(millis: Long): String {
    val totalSeconds = abs(millis) / 1000
    val text = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    return if (millis < 0) "-$text" else text
}

fun formatRpe(rpe: Double?): String = rpe?.let(::formatWeight) ?: "—"

fun nextRpe(rpe: Double?): Double = rpe?.let { (it + RPE_STEP).coerceAtMost(RPE_MAX) } ?: RPE_MIN

fun previousRpe(rpe: Double?): Double? = rpe?.takeIf { it > RPE_MIN }?.minus(RPE_STEP)
