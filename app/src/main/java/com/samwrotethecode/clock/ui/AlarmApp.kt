package com.samwrotethecode.clock.ui

import android.app.Application
import com.samwrotethecode.clock.data.AlarmAppContainer
import com.samwrotethecode.clock.data.AppContainer

class AlarmApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AlarmAppContainer(this)
    }
}