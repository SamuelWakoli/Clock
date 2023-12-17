package com.samwrotethecode.clock.alarm_scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.samwrotethecode.clock.data.AlarmDatabase
import com.samwrotethecode.clock.data.AlarmOfflineRepository
import com.samwrotethecode.clock.data.AlarmRepository

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var alarmRepository: AlarmRepository
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context != null)
        alarmRepository  = AlarmOfflineRepository(AlarmDatabase.getDatabase(context = context).alarmDao())


        // TODO: Check if it repeats, vibration
        /// if it repeats, reschedule

        Log.d("ALARM SCHEDULER", "RECEIVER CALLED")
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "No label"
        val alarmId = intent?.getStringExtra("ALARM_ID") ?: "No ID"


        Log.d("ALARM SCHEDULER", "ALARM RECEIVED, ID: $alarmId Label:, $alarmLabel")
    }
}
