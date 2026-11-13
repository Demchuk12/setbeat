package com.vadem.setbeat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vadem.setbeat.SetBeatApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SetBeatApplication).container
        setContent { SetBeatApp(container) }
    }
}
