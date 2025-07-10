package com.samwrotethecode.clock.alarm_scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

interface AlarmScheduler {
    fun scheduleAlarm(alarmItem: AlarmDatabaseItem)
    fun cancelAlarm(alarmItem: AlarmDatabaseItem)
}

class AppAlarmScheduler(private val context: Context) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    private fun calculateNextTriggerTime(alarmItem: AlarmDatabaseItem): ZonedDateTime? {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var alarmTime = LocalTime.of(alarmItem.hour, alarmItem.minute)
        var alarmDateTime = LocalDateTime.of(now.toLocalDate(), alarmTime)

        // If it's a repeating alarm
        if (alarmItem.days.contains("1")) {
            var scheduled = false
            for (i in 0..6) { // Check next 7 days starting from today
                val checkingDate = now.toLocalDate().plusDays(i.toLong())
                val dayOfWeek = checkingDate.dayOfWeek // MONDAY (1) to SUNDAY (7)

                // Check if this day is selected in alarmItem.days (0 is Monday, 6 is Sunday)
                if (alarmItem.days[dayOfWeek.value - 1] == '1') {
                    alarmDateTime = LocalDateTime.of(checkingDate, alarmTime)
                    if (ZonedDateTime.of(alarmDateTime, ZoneId.systemDefault()).isAfter(now)) {
                        scheduled = true
                        break
                    }
                }
            }
            // If all selected days in the current week + today have passed, find the next occurrence in the following week
            if (!scheduled) {
                for (i in 0..6) {
                    val checkingDate = now.toLocalDate().plusDays(7 + i.toLong()) // Start from next week
                    val dayOfWeek = checkingDate.dayOfWeek
                    if (alarmItem.days[dayOfWeek.value - 1] == '1') {
                        alarmDateTime = LocalDateTime.of(checkingDate, alarmTime)
                        break // Found the first available day next week
                    }
                }
            }

        } else { // One-time alarm
            if (ZonedDateTime.of(alarmDateTime, ZoneId.systemDefault()).isBefore(now) ||
                ZonedDateTime.of(alarmDateTime, ZoneId.systemDefault()).isEqual(now)
            ) {
                alarmDateTime = alarmDateTime.plusDays(1) // Schedule for tomorrow
            }
        }
        return ZonedDateTime.of(alarmDateTime, ZoneId.systemDefault())
    }

    override fun scheduleAlarm(alarmItem: AlarmDatabaseItem) {
        if (!alarmItem.isActive) {
            Log.d("AppAlarmScheduler", "Alarm ${alarmItem.id} is not active. Not scheduling.")
            return
        }

        // Permission Check for Android S+ is no longer needed as minSdk is 31
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w("AppAlarmScheduler", "Missing SCHEDULE_EXACT_ALARM permission.")
            Toast.makeText(context, "Permission needed to schedule alarms. Please grant it in app settings.", Toast.LENGTH_LONG).show()
            // Optionally, direct user to settings: Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            return
        }

        val triggerTime = calculateNextTriggerTime(alarmItem)

        if (triggerTime == null) {
            Log.e("AppAlarmScheduler", "Could not calculate next trigger time for alarm ${alarmItem.id}")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM // Use the new action
            putExtra("ALARM_ID", alarmItem.id)
            putExtra("ALARM_LABEL", alarmItem.label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmItem.id, // Use alarm ID as request code for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime.toInstant().toEpochMilli(),
                pendingIntent
            )
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            Toast.makeText(
                context,
                "Alarm '${alarmItem.label ?: ""}' scheduled for ${triggerTime.format(formatter)}",
                Toast.LENGTH_LONG
            ).show()
            Log.d(
                "AppAlarmScheduler",
                "Alarm id: ${alarmItem.id} scheduled for ${triggerTime.format(formatter)}"
            )
        } catch (se: SecurityException) {
            Log.e("AppAlarmScheduler", "SecurityException while scheduling alarm. Check WAKE_LOCK permission?", se)
            Toast.makeText(context, "Could not schedule alarm due to security restrictions.", Toast.LENGTH_LONG).show()
        }
    }

    override fun cancelAlarm(alarmItem: AlarmDatabaseItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM // Action must match for cancellation
             putExtra("ALARM_ID", alarmItem.id) // Ensure ID is present for cancellation to work if PI matching needs it
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmItem.id, // Request code must match
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AppAlarmScheduler", "Cancelled alarm id: ${alarmItem.id}")
        Toast.makeText(context, "Alarm '${alarmItem.label ?: ""}' cancelled", Toast.LENGTH_SHORT).show()
    }
}
