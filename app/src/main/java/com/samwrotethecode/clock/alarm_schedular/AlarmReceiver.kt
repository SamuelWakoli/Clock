package com.samwrotethecode.clock.alarm_schedular

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ALARM SCHEDULER", "RECEIVER CALLED")
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: return


        Log.d("ALARM SCHEDULER", "ALARM RECEIVED:, $alarmLabel")
    }
}
