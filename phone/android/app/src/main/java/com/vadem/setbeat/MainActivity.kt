package com.vadem.setbeat

import android.os.Bundle
import com.getcapacitor.BridgeActivity
import com.vadem.setbeat.wear.WearSyncPlugin

class MainActivity : BridgeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(WearSyncPlugin::class.java)
        super.onCreate(savedInstanceState)
    }
}
