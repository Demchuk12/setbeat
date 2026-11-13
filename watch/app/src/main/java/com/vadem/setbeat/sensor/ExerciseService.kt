package com.vadem.setbeat.sensor

import android.content.Intent
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseUpdate
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.vadem.setbeat.SetBeatApplication
import com.vadem.setbeat.domain.HeartRateSample
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG = "SetBeat"

class ExerciseService : LifecycleService() {

    private val container by lazy { (application as SetBeatApplication).container }
    private val exercise by lazy { container.healthServicesExercise }
    private var sessionId: String? = null
    private var finishing = false

    private val callback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val samples = update.heartRateSamples()
            samples.lastOrNull()?.let { latest ->
                container.heartRateMonitor.update {
                    it.copy(bpm = latest.bpm.roundToInt(), sensorStatus = latest.accuracy, unavailable = false)
                }
            }
            val state = update.exerciseStateInfo.state
            lifecycleScope.launch {
                record(samples)
                if (state.isEnded) finish("health services: ${state.name}")
            }
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit

        override fun onRegistered() = Unit

        override fun onRegistrationFailed(throwable: Throwable) {
            Log.e(TAG, "callback registration failed", throwable)
        }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            Log.d(TAG, "availability ${dataType.name} $availability")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            HealthServicesTracker.ACTION_TRACK -> intent.getStringExtra(HealthServicesTracker.EXTRA_SESSION_ID)?.let(::track) ?: stopSelf()
            HealthServicesTracker.ACTION_STOP -> lifecycleScope.launch { stopTracking() }
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun track(id: String) {
        ServiceCompat.startForeground(this, WorkoutNotification.ID, WorkoutNotification.build(this), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        sessionId = id
        finishing = false
        lifecycleScope.launch {
            val stored = container.heartRateRepository.count(id)
            container.heartRateMonitor.update { it.copy(storedSamples = stored) }
            try {
                val started = exercise.attachOrStart(callback)
                Log.d(TAG, "tracking session=$id started=$started")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "heart rate unavailable", e)
                container.heartRateMonitor.update { it.copy(unavailable = true) }
            }
        }
    }

    private suspend fun stopTracking() {
        val ended = runCatching { exercise.end() }.onFailure { Log.e(TAG, "end exercise failed", it) }.getOrDefault(false)
        if (!ended) finish("health services: not running")
    }

    private suspend fun record(samples: List<HeartRateSample>) {
        val id = sessionId ?: return
        container.heartRateRepository.save(id, samples)
        container.heartRateMonitor.update { it.copy(storedSamples = it.storedSamples + samples.size) }
    }

    private suspend fun finish(reason: String) {
        if (finishing) return
        finishing = true
        runCatching { exercise.detach(callback) }
        container.trackingListener.onTrackingEnded(reason)
        sessionId = null
        container.heartRateMonitor.reset()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
