package com.vadem.setbeat.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import java.io.File

object WearModule {
    fun store(context: Context) = SessionStore(File(context.filesDir, "sessions"))

    fun receiver(context: Context) = DataLayerSessionReceiver(
        dataClient = Wearable.getDataClient(context),
        store = store(context),
        onError = { Log.e("SetBeat", "session receive failed", it) },
    )
}
