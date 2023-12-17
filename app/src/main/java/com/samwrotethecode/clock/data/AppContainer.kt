package com.samwrotethecode.clock.data

import android.content.Context
import com.samwrotethecode.clock.alarm_scheduler.AlarmScheduler
import com.samwrotethecode.clock.alarm_scheduler.AppAlarmScheduler

interface AppContainer {
    val alarmRepository: AlarmRepository
    val alarmScheduler: AlarmScheduler
}

class AlarmAppContainer(context: Context) : AppContainer {
    override val alarmRepository: AlarmRepository =
        AlarmOfflineRepository(AlarmDatabase.getDatabase(context).alarmDao())

    override val alarmScheduler: AlarmScheduler =
        AppAlarmScheduler(context = context)
}