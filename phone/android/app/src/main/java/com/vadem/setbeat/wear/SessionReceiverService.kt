package com.vadem.setbeat.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking

class SessionReceiverService : WearableListenerService() {
    private val receiver by lazy { WearModule.receiver(this) }

    override fun onDataChanged(events: DataEventBuffer) {
        val items = events.filter { it.type == DataEvent.TYPE_CHANGED }.map { it.dataItem.freeze() }
        runBlocking { items.forEach { receiver.receiveSafely(it) } }
    }
}
