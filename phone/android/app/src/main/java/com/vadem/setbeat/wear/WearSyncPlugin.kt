package com.vadem.setbeat.wear

import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@CapacitorPlugin(name = "WearSync")
class WearSyncPlugin : Plugin() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var store: SessionStore
    private lateinit var receiver: DataLayerSessionReceiver
    private val onReceived: (String) -> Unit = { id -> notifyListeners("sessionReceived", JSObject().put("id", id)) }

    override fun load() {
        store = WearModule.store(context)
        receiver = WearModule.receiver(context)
        SessionEvents.subscribe(onReceived)
    }

    override fun handleOnDestroy() {
        SessionEvents.unsubscribe(onReceived)
        scope.cancel()
    }

    @PluginMethod
    fun listSessions(call: PluginCall) {
        val sessions = JSArray()
        store.list().forEach { s ->
            sessions.put(
                JSObject()
                    .put("id", s.id)
                    .put("startedAt", s.startedAt)
                    .put("endedAt", s.endedAt)
                    .put("receivedAt", s.receivedAt)
            )
        }
        call.resolve(JSObject().put("sessions", sessions))
    }

    @PluginMethod
    fun getSession(call: PluginCall) {
        val id = call.getString("id") ?: return call.reject("id is required")
        val json = store.read(id) ?: return call.reject("session $id not found")
        call.resolve(JSObject().put("json", json))
    }

    @PluginMethod
    fun syncNow(call: PluginCall) {
        scope.launch {
            runCatching { receiver.pullPending() }
                .onSuccess { call.resolve(JSObject().put("available", true).put("received", it)) }
                .onFailure { e ->
                    if (e.isWearableMissing()) call.resolve(JSObject().put("available", false).put("received", 0))
                    else call.reject(e.message, e as? Exception)
                }
        }
    }

    @PluginMethod
    fun connectedNodes(call: PluginCall) {
        scope.launch {
            runCatching { Wearable.getNodeClient(context).connectedNodes.await() }
                .onSuccess { nodes ->
                    val result = JSArray()
                    nodes.forEach { result.put(JSObject().put("id", it.id).put("name", it.displayName).put("nearby", it.isNearby)) }
                    call.resolve(JSObject().put("available", true).put("nodes", result))
                }
                .onFailure { e ->
                    if (e.isWearableMissing()) call.resolve(JSObject().put("available", false).put("nodes", JSArray()))
                    else call.reject(e.message, e as? Exception)
                }
        }
    }

    private fun Throwable.isWearableMissing() =
        this is ApiException && statusCode == CommonStatusCodes.API_NOT_CONNECTED
}
