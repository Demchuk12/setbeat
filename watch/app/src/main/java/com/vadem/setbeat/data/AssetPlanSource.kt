package com.vadem.setbeat.data

import android.content.Context
import com.vadem.setbeat.domain.WorkoutPlan
import com.vadem.setbeat.workout.PlanSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class AssetPlanSource(private val context: Context) : PlanSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load(): WorkoutPlan = withContext(Dispatchers.IO) {
        val custom = File(context.getExternalFilesDir(null), CUSTOM_PLAN)
        val text = if (custom.exists()) {
            custom.readText()
        } else {
            context.assets.open(BUNDLED_PLAN).bufferedReader().use { it.readText() }
        }
        json.decodeFromString(WorkoutPlan.serializer(), text)
    }

    private companion object {
        const val CUSTOM_PLAN = "plan.json"
        const val BUNDLED_PLAN = "plan.json"
    }
}
