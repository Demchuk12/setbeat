package com.vadem.setbeat.platform

import android.content.Context
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.VibratorManager
import com.vadem.setbeat.workout.RestTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VibratingRestTimer(context: Context, private val scope: CoroutineScope) : RestTimer {
    private val vibrator = context.getSystemService(VibratorManager::class.java).defaultVibrator
    private val wakeLock = context.getSystemService(PowerManager::class.java)
        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "setbeat:rest")
        .apply { setReferenceCounted(false) }
    private var job: Job? = null

    override fun schedule(remainingMillis: Long) {
        cancel()
        if (remainingMillis <= 0) return
        wakeLock.acquire(remainingMillis + WAKE_LOCK_MARGIN_MS)
        job = scope.launch {
            try {
                delay(remainingMillis)
                vibrator.vibrate(VibrationEffect.createWaveform(PATTERN, -1))
            } finally {
                releaseWakeLock()
            }
        }
    }

    override fun cancel() {
        job?.cancel()
        job = null
        releaseWakeLock()
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) wakeLock.release()
    }

    private companion object {
        const val WAKE_LOCK_MARGIN_MS = 5_000L
        val PATTERN = longArrayOf(0, 400, 200, 400, 200, 700)
    }
}
