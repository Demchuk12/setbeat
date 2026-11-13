package com.vadem.setbeat.platform

import android.util.Log
import com.vadem.setbeat.workout.Logger

class AndroidLogger(private val tag: String) : Logger {
    override fun info(message: String) {
        Log.d(tag, message)
    }

    override fun warn(message: String, error: Throwable?) {
        Log.w(tag, message, error)
    }
}
