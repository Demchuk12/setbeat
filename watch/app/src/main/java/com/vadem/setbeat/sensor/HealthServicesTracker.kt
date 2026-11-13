package com.vadem.setbeat.sensor

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.vadem.setbeat.workout.ExerciseTracker

class HealthServicesTracker(private val context: Context) : ExerciseTracker {

    override fun start(sessionId: String) = track(sessionId)

    override fun resume(sessionId: String) = track(sessionId)

    override fun stop() {
        runCatching { context.startService(intent(ACTION_STOP)) }
            .onFailure { Log.e("SetBeat", "stop request failed", it) }
    }

    private fun track(sessionId: String) {
        ContextCompat.startForegroundService(context, intent(ACTION_TRACK).putExtra(EXTRA_SESSION_ID, sessionId))
    }

    private fun intent(action: String) = Intent(context, ExerciseService::class.java).setAction(action)

    companion object {
        const val ACTION_TRACK = "com.vadem.setbeat.TRACK"
        const val ACTION_STOP = "com.vadem.setbeat.STOP"
        const val EXTRA_SESSION_ID = "session_id"
    }
}
