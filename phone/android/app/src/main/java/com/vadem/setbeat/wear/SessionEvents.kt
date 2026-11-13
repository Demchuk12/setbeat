package com.vadem.setbeat.wear

import java.util.concurrent.CopyOnWriteArraySet

object SessionEvents {
    private val listeners = CopyOnWriteArraySet<(String) -> Unit>()

    fun subscribe(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: (String) -> Unit) {
        listeners.remove(listener)
    }

    fun received(sessionId: String) {
        listeners.forEach { it(sessionId) }
    }
}
