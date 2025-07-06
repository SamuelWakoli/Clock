package com.samwrotethecode.clock.alarm_scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.samwrotethecode.clock.data.AlarmDatabase
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import com.samwrotethecode.clock.data.AlarmOfflineRepository
import com.samwrotethecode.clock.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var alarmScheduler: AlarmScheduler // Added for rescheduling
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private var currentRingtone: Ringtone? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        alarmRepository =
            AlarmOfflineRepository(AlarmDatabase.getDatabase(context = context).alarmDao())
        alarmScheduler = AppAlarmScheduler(context) // Initialize scheduler

        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"

        if (alarmId == -1) {
            Log.e("ALARM SCHEDULER", "Alarm ID missing or invalid in intent")
            return
        }

        Log.d("ALARM SCHEDULER", "RECEIVER CALLED for ID: $alarmId, Label: $alarmLabel")

        scope.launch {
            val alarmItem: AlarmDatabaseItem? = alarmRepository.getAlarm(alarmId).firstOrNull()

            if (alarmItem == null) {
                Log.e("ALARM SCHEDULER", "Alarm item with ID $alarmId not found in database.")
                return@launch
            }

            Log.d("ALARM SCHEDULER", "Retrieved alarm item: $alarmItem")

            // Play the sound only if the alarm is currently active
            if (alarmItem.isActive) {
                currentRingtone?.stop()

                val toneUriString = alarmItem.toneUri
                val ringtoneUri: Uri? = if (toneUriString != null) {
                    try {
                        toneUriString.toUri()
                    } catch (e: Exception) {
                        Log.e("ALARM SCHEDULER", "Failed to parse tone URI: $toneUriString", e)
                        null
                    }
                } else {
                    null
                }

                currentRingtone = if (ringtoneUri != null) {
                    RingtoneManager.getRingtone(context, ringtoneUri)
                } else {
                    val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    if (defaultAlarmUri != null) {
                        RingtoneManager.getRingtone(context, defaultAlarmUri)
                    } else {
                        val defaultNotificationUri =
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        RingtoneManager.getRingtone(context, defaultNotificationUri)
                    }
                }

                if (currentRingtone == null) {
                    Log.e("ALARM SCHEDULER", "Failed to get any ringtone.")
                } else {
                    try {
                        currentRingtone?.play()
                        Log.d(
                            "ALARM SCHEDULER",
                            "Playing ringtone: ${currentRingtone?.getTitle(context)}"
                        )
                    } catch (e: Exception) {
                        Log.e("ALARM SCHEDULER", "Error playing ringtone", e)
                    }
                }

                // Rescheduling logic
                val isRepeating = alarmItem.days.contains("1")
                if (isRepeating) {
                    // It's a repeating alarm and it's active, so reschedule it for the next occurrence.
                    alarmScheduler.scheduleAlarm(alarmItem)
                    Log.d("ALARM SCHEDULER", "Rescheduled repeating alarm ID: ${alarmItem.id}")
                } else {
                    // It's a one-time alarm, so mark it as inactive.
                    val updatedAlarmItem = alarmItem.copy(isActive = false)
                    alarmRepository.updateAlarm(updatedAlarmItem)
                    Log.d(
                        "ALARM SCHEDULER",
                        "Marked one-time alarm ID: ${alarmItem.id} as inactive."
                    )
                }
            } else {
                Log.d(
                    "ALARM SCHEDULER",
                    "Alarm ID: ${alarmItem.id} is inactive. Not playing or rescheduling."
                )
            }
        }
    }
}
