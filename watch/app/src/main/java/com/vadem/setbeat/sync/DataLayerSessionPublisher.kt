package com.vadem.setbeat.sync

import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class DataLayerSessionPublisher(private val dataClient: DataClient) {

    suspend fun publish(sessionId: String, file: File) {
        val bytes = withContext(Dispatchers.IO) { file.readBytes() }
        val request = PutDataMapRequest.create("$PATH_PREFIX$sessionId").apply {
            dataMap.putString(KEY_ID, sessionId)
            dataMap.putAsset(KEY_SESSION, Asset.createFromBytes(bytes))
        }.asPutDataRequest().setUrgent()
        dataClient.putDataItem(request).await()
    }

    companion object {
        const val PATH_PREFIX = "/sessions/"
        const val KEY_ID = "id"
        const val KEY_SESSION = "session"
    }
}
