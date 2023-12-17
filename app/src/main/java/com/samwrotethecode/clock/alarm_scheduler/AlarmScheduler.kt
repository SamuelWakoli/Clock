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
            putExtra("ALARM_ID", alarmItem.id)
        }

        val currentTime = ZonedDateTime.now()
        val alarmData = alarmItem.copy(
            hour = alarmItem.hour - currentTime.hour,
            minute = alarmItem.minute - currentTime.minute,
        )

        if (alarmData.isActive) {
            alarmManager.setExactAndAllowWhileIdle(
                /* type = */ AlarmManager.RTC_WAKEUP,
                /* triggerAtMillis = */
                ZonedDateTime.now().toEpochSecond() * 1000 + // this is the current time
                        ((alarmData.hour * 60 * 60 + alarmData.minute * 60) * 1000).toLong(), // + the set time

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