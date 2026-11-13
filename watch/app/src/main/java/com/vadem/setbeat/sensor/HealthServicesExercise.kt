package com.vadem.setbeat.sensor

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.BatchingMode
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseCapabilities
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.HeartRateAccuracy
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.getCurrentExerciseInfo
import androidx.health.services.client.startExercise
import com.vadem.setbeat.domain.HeartRateSample
import java.time.Instant

class HeartRateUnsupported : IllegalStateException("heart rate is not supported for strength training")

class HealthServicesExercise(private val client: ExerciseClient) {

    suspend fun attachOrStart(callback: ExerciseUpdateCallback): Boolean {
        client.setUpdateCallback(callback)
        if (isOwnExerciseInProgress()) return false
        val capabilities = client.getCapabilities()
        val type = supportedType(capabilities) ?: throw HeartRateUnsupported()
        val batching = setOf(BatchingMode.HEART_RATE_5_SECONDS).intersect(capabilities.supportedBatchingModeOverrides)
        client.startExercise(
            ExerciseConfig.builder(type)
                .setDataTypes(setOf(DataType.HEART_RATE_BPM))
                .setIsAutoPauseAndResumeEnabled(false)
                .setIsGpsEnabled(false)
                .setBatchingModeOverrides(batching)
                .build()
        )
        return true
    }

    suspend fun end(): Boolean {
        if (!isOwnExerciseInProgress()) return false
        client.endExercise()
        return true
    }

    suspend fun detach(callback: ExerciseUpdateCallback) {
        client.clearUpdateCallback(callback)
    }

    @SuppressLint("RestrictedApi")
    private suspend fun isOwnExerciseInProgress(): Boolean =
        client.getCurrentExerciseInfo().exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS

    private fun supportedType(capabilities: ExerciseCapabilities): ExerciseType? =
        listOf(ExerciseType.STRENGTH_TRAINING, ExerciseType.WEIGHTLIFTING).firstOrNull { type ->
            type in capabilities.supportedExerciseTypes &&
                DataType.HEART_RATE_BPM in capabilities.getExerciseTypeCapabilities(type).supportedDataTypes
        }
}

fun ExerciseUpdate.heartRateSamples(): List<HeartRateSample> {
    val bootInstant = Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime())
    return latestMetrics.getData(DataType.HEART_RATE_BPM).map { point ->
        HeartRateSample(
            timestamp = point.getTimeInstant(bootInstant).toEpochMilli(),
            bpm = point.value,
            accuracy = (point.accuracy as? HeartRateAccuracy)?.sensorStatus?.name,
        )
    }
}
