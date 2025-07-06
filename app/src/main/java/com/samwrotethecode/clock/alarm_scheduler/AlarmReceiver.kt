package com.samwrotethecode.clock.alarm_scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
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
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private var currentRingtone: Ringtone? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        alarmRepository = AlarmOfflineRepository(AlarmDatabase.getDatabase(context = context).alarmDao())

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

            currentRingtone?.stop()

            val toneUriString = alarmItem.toneUri
            val ringtoneUri: Uri? = if (toneUriString != null) {
                try {
                    Uri.parse(toneUriString)
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
                    val defaultNotificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    RingtoneManager.getRingtone(context, defaultNotificationUri)
                }
            }

            if (currentRingtone == null) {
                Log.e("ALARM SCHEDULER", "Failed to get any ringtone.")
            } else {
                try {
                    currentRingtone?.play()
                    Log.d("ALARM SCHEDULER", "Playing ringtone: ${currentRingtone?.getTitle(context)}")
                } catch (e: Exception) {
                    Log.e("ALARM SCHEDULER", "Error playing ringtone", e)
                }
            }
        }
    }
}
