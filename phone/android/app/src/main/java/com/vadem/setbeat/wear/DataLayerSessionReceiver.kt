package com.vadem.setbeat.wear

import android.net.Uri
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class DataLayerSessionReceiver(
    private val dataClient: DataClient,
    private val store: SessionStore,
    private val onError: (Throwable) -> Unit,
) {

    suspend fun receive(item: DataItem): Boolean {
        val path = item.uri.path ?: return false
        if (!path.startsWith(PATH_PREFIX)) return false
        val map = DataMapItem.fromDataItem(item).dataMap
        val id = map.getString(KEY_ID) ?: path.removePrefix(PATH_PREFIX)
        val asset = map.getAsset(KEY_SESSION) ?: return false
        val bytes = dataClient.getFdForAsset(asset).await().inputStream.use { it.readBytes() }
        val isNew = store.save(id, bytes)
        dataClient.deleteDataItems(item.uri).await()
        if (isNew) SessionEvents.received(id)
        return isNew
    }

    suspend fun pullPending(): Int {
        val uri = Uri.Builder().scheme("wear").path(PATH_PREFIX).build()
        val buffer = dataClient.getDataItems(uri, DataClient.FILTER_PREFIX).await()
        val items = try {
            buffer.map { it.freeze() }
        } finally {
            buffer.release()
        }
        return items.count { receiveSafely(it) }
    }

    suspend fun receiveSafely(item: DataItem): Boolean = try {
        receive(item)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        onError(e)
        false
    }

    private companion object {
        const val PATH_PREFIX = "/sessions/"
        const val KEY_ID = "id"
        const val KEY_SESSION = "session"
    }
}
