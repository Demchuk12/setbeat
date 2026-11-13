package com.vadem.setbeat

import android.content.Context
import androidx.health.services.client.HealthServices
import com.google.android.gms.wearable.Wearable
import com.vadem.setbeat.data.AssetPlanSource
import com.vadem.setbeat.data.DeviceIdProvider
import com.vadem.setbeat.data.HeartRateRepository
import com.vadem.setbeat.data.RoomWorkoutEventStore
import com.vadem.setbeat.data.db.SetBeatDatabase
import com.vadem.setbeat.data.export.SessionExporter
import com.vadem.setbeat.platform.AndroidLogger
import com.vadem.setbeat.platform.VibratingRestTimer
import com.vadem.setbeat.sensor.HealthServicesExercise
import com.vadem.setbeat.sensor.HealthServicesTracker
import com.vadem.setbeat.sensor.HeartRateMonitor
import com.vadem.setbeat.sync.DataLayerSessionPublisher
import com.vadem.setbeat.workout.SessionArchive
import com.vadem.setbeat.workout.TrackingListener
import com.vadem.setbeat.workout.WorkoutController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.MainScope
import java.io.File

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = MainScope()
    private val logger = AndroidLogger("SetBeat")
    private val database = SetBeatDatabase.create(appContext)
    private val deviceId = DeviceIdProvider(appContext)

    val heartRateMonitor = HeartRateMonitor()
    val heartRateRepository = HeartRateRepository(database.heartRateSampleDao())
    val healthServicesExercise = HealthServicesExercise(HealthServices.getClient(appContext).exerciseClient)

    private val exporter = SessionExporter(
        sessions = database.sessionDao(),
        events = database.workoutEventDao(),
        samples = database.heartRateSampleDao(),
        directory = File(appContext.getExternalFilesDir(null), "sessions"),
    )
    private val publisher = DataLayerSessionPublisher(Wearable.getDataClient(appContext))

    private val archive = SessionArchive { sessionId ->
        try {
            val file = exporter.export(sessionId) ?: return@SessionArchive
            publisher.publish(sessionId, file)
            logger.info("archived session=$sessionId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn("archive failed session=$sessionId", e)
        }
    }

    val workoutController = WorkoutController(
        scope = scope,
        store = RoomWorkoutEventStore(database.sessionDao(), database.workoutEventDao()) { deviceId.id },
        plans = AssetPlanSource(appContext),
        tracker = HealthServicesTracker(appContext),
        restTimer = VibratingRestTimer(appContext, scope),
        archive = archive,
        clock = { System.currentTimeMillis() },
        logger = logger,
    )

    val trackingListener: TrackingListener get() = workoutController
}
