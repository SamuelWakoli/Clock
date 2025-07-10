package com.samwrotethecode.clock.alarm_scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISMISS_ALARM = "com.samwrotethecode.clock.ACTION_DISMISS_ALARM"
        const val ACTION_TRIGGER_ALARM = "com.samwrotethecode.clock.ACTION_TRIGGER_ALARM"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e("AlarmReceiver", "Context or Intent is null")
            return
        }

        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        if (alarmId == -1 && intent.action != AlarmService.ACTION_SNOOZE_ALARM) { // Snooze might not have an ID initially if it's a generic snooze
            Log.e("AlarmReceiver", "Alarm ID missing or invalid in intent action: ${intent.action}")
            return
        }

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            // Pass the original intent action to the service, or define a new one for clarity
            // For example, AlarmService could have its own set of actions like START, DISMISS, SNOOZE
            this.action = intent.action // Forward the action (e.g., TRIGGER_ALARM, DISMISS_ALARM)
            // If the original intent has a label, forward it too.
            if (intent.hasExtra("ALARM_LABEL")) {
                putExtra("ALARM_LABEL", intent.getStringExtra("ALARM_LABEL"))
            }
        }

        Log.d(
            "AlarmReceiver",
            "Received action: ${intent.action} for alarm ID: $alarmId. Starting service."
        )

        // For triggering an alarm, it must be a foreground service.
        // For dismissing, a regular startService is fine, but for consistency and potential future
        // operations during dismiss, startForegroundService can also be used if the service
        // calls startForeground() quickly.

        context.startForegroundService(serviceIntent)

    }
}