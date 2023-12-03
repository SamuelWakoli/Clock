package com.samwrotethecode.clock.data

import android.content.Context

interface AppContainer {
    val alarmRepository: AlarmRepository
}

class AlarmAppContainer(context: Context) : AppContainer {
    override val alarmRepository: AlarmRepository =
        AlarmOfflineRepository(AlarmDatabase.getDatabase(context).alarmDao())
}