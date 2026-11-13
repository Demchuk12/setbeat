package com.vadem.setbeat

import android.app.Application

class SetBeatApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
