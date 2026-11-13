package com.vadem.setbeat.data

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

class DeviceIdProvider(context: Context) {
    private val prefs = context.getSharedPreferences("setbeat", Context.MODE_PRIVATE)

    val id: String by lazy {
        prefs.getString(KEY, null) ?: UUID.randomUUID().toString().also { prefs.edit { putString(KEY, it) } }
    }

    private companion object {
        const val KEY = "device_id"
    }
}
