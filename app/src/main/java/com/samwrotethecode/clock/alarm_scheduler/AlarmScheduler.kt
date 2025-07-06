package com.samwrotethecode.clock.alarm_scheduler

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import java.time.ZonedDateTime

interface AlarmScheduler {
    fun scheduleAlarm(alarmItem: AlarmDatabaseItem)
    fun cancelAlarm(alarmItem: AlarmDatabaseItem)
}

/**
 * Scheduler for alarms.
 * @WARNING: This class contains bugs when scheduling alarms.
 *
 *
 * @param context The context of the application.
 * @author Sam
 *
 * @see AlarmReceiver
 * @see AlarmDatabaseItem
 * @constructor Creates an AlarmScheduler object.
 *
 */

class AppAlarmScheduler(private val context: Context) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)


    @SuppressLint("MissingPermission")
    override fun scheduleAlarm(alarmItem: AlarmDatabaseItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_LABEL", alarmItem.label)
            putExtra("ALARM_ID", alarmItem.id) // alarmItem.id is already an Int
        }

        val currentTime = ZonedDateTime.now()
        // BUG: The logic for calculating relative alarmData hour/minute is likely flawed
        // and will not work correctly for scheduling across day boundaries or for specific dates.
        // It should calculate the next occurrence of alarmItem.hour and alarmItem.minute.
        val alarmData = alarmItem.copy(
            hour = alarmItem.hour - currentTime.hour, // This relative calculation is problematic
            minute = alarmItem.minute - currentTime.minute, // This relative calculation is problematic
        )

        // The triggerAtMillis calculation also seems to be based on this relative time,
        // which needs to be corrected to an absolute future timestamp.
        // It should use Calendar or java.time to set a specific time for the alarm.

        if (alarmItem.isActive) { // Use alarmItem.isActive directly
            alarmManager.setExactAndAllowWhileIdle(
                /* type = */ AlarmManager.RTC_WAKEUP,
                /* triggerAtMillis = */
                ZonedDateTime.now().toEpochSecond() * 1000 +
                        ((alarmData.hour * 60 * 60 + alarmData.minute * 60) * 1000).toLong(), // Problematic time calculation

                /* operation = */
                PendingIntent.getBroadcast(
                    context,
                    alarmItem.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            Toast.makeText(
                context,
                // Displaying relative time here can be misleading if the scheduling logic is fixed
                "Alarm Scheduled in ${alarmData.hour} hours, ${alarmData.minute} minutes",
                Toast.LENGTH_LONG
            ).show()

            Log.d(
                "ALARM SCHEDULER",
                "Alarm id: ${alarmItem.id}, Scheduled in ${alarmData.hour} hours, ${alarmData.minute} minutes"
            )
        }
    }

    override fun cancelAlarm(alarmItem: AlarmDatabaseItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                alarmItem.id,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        Log.d(
            "ALARM SCHEDULER",
            "Alarm id: ${alarmItem.id} CANCELLED"
        )
    }
}