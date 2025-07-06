package com.samwrotethecode.clock.alarm_scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.samwrotethecode.clock.R
import com.samwrotethecode.clock.data.AlarmDatabase
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import com.samwrotethecode.clock.data.AlarmOfflineRepository
import com.samwrotethecode.clock.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AlarmService : Service() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var alarmScheduler: AppAlarmScheduler

    companion object {
        const val TAG = "AlarmService"

        const val ACTION_TRIGGER_ALARM = "com.samwrotethecode.clock.ACTION_TRIGGER_ALARM"
        const val ACTION_DISMISS_ALARM = "com.samwrotethecode.clock.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.samwrotethecode.clock.ACTION_SNOOZE_ALARM"

        const val EXTRA_ALARM_ID = "ALARM_ID"
        const val EXTRA_ALARM_LABEL = "ALARM_LABEL" // Though label can be fetched from DB

        private const val CHANNEL_ID = "alarm_clock_channel" // Must match MainActivity
        private const val SNOOZE_DURATION_MINUTES = 10L

        private var currentRingtone: Ringtone? = null
        private var currentAlarmId: Int = -1

        fun stopCurrentRingtone() {
            try {
                currentRingtone?.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping ringtone: ", e)
            }
            currentRingtone = null
            currentAlarmId = -1
        }
    }

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmOfflineRepository(AlarmDatabase.getDatabase(this).alarmDao())
        alarmScheduler = AppAlarmScheduler(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Log.w(TAG, "Service started with null intent. Stopping.")
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val action = intent.action
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        val alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "Alarm"

        Log.d(TAG, "onStartCommand received action: $action for alarmId: $alarmId")

        when (action) {
            ACTION_TRIGGER_ALARM -> {
                if (alarmId != -1) {
                    handleTriggerAlarm(alarmId)
                } else {
                    Log.e(TAG, "TRIGGER_ALARM action received with invalid alarmId. Stopping.")
                    stopSelf(startId)
                }
            }

            ACTION_DISMISS_ALARM -> {
                handleDismissAlarm(alarmId, startId)
            }

            ACTION_SNOOZE_ALARM -> {
                if (alarmId != -1) {
                    handleSnoozeAlarm(alarmId, alarmLabel, startId)
                } else {
                    Log.e(TAG, "SNOOZE_ALARM action received with invalid alarmId. Stopping.")
                    stopSelf(startId)
                }
            }

            else -> {
                Log.w(TAG, "Unknown action received: $action. Stopping.")
                stopSelf(startId)
            }
        }
        return START_STICKY // Keep service running until explicitly stopped
    }

    private fun handleTriggerAlarm(alarmId: Int) {
        serviceScope.launch {
            val alarmItem = alarmRepository.getAlarm(alarmId).firstOrNull()
            if (alarmItem == null || !alarmItem.isActive) {
                Log.w(
                    TAG,
                    "Alarm $alarmId not found or not active. Stopping service for this trigger."
                )
                // If this specific alarm instance was meant to start the service, stop it.
                // However, another alarm might be ringing, so check currentAlarmId.
                if (currentAlarmId == alarmId || currentAlarmId == -1) {
                    stopSelf() // Use stopSelf() without startId as it's in a coroutine
                }
                return@launch
            }

            Log.d(TAG, "Triggering alarm: ${alarmItem.label ?: "No Label"} (ID: $alarmId)")
            stopCurrentRingtone() // Stop any previous ringtone
            currentAlarmId = alarmId
            startRingtone(alarmItem.toneUri)
            showNotification(alarmId, alarmItem.label ?: "Alarm")

            // Reschedule or deactivate
            if (alarmItem.days.contains("1")) { // Repeating alarm
                alarmScheduler.scheduleAlarm(alarmItem)
                Log.d(TAG, "Rescheduled repeating alarm $alarmId")
            } else { // One-time alarm
                alarmRepository.updateAlarm(alarmItem.copy(isActive = false))
                Log.d(TAG, "Deactivated one-time alarm $alarmId")
            }
        }
    }

    private fun handleDismissAlarm(alarmId: Int, startId: Int) {
        Log.d(TAG, "Dismissing alarm: $alarmId")
        stopCurrentRingtone()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId) // Use alarmId as notification ID

        // Consider stopping the service only if this was the alarm that kept it alive
        // For simplicity, we stop if any dismiss comes. If multiple alarms use the same service instance,
        // this might stop it prematurely for other active alarms.
        // A more robust solution involves reference counting or ensuring one service instance per alarm.
        stopSelf(startId)
    }

    private fun handleSnoozeAlarm(alarmId: Int, alarmLabel: String, startId: Int) {
        Log.d(TAG, "Snoozing alarm: $alarmId")
        stopCurrentRingtone()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId)

        serviceScope.launch {
            val originalAlarm = alarmRepository.getAlarm(alarmId).firstOrNull()
            if (originalAlarm != null) {
                // Create a temporary alarm item for snooze
                val snoozeTimeMillis =
                    System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(SNOOZE_DURATION_MINUTES)
                val snoozeDateTime = java.time.Instant.ofEpochMilli(snoozeTimeMillis)
                    .atZone(java.time.ZoneId.systemDefault())

                // Create a temporary, non-repeating alarm item for the snooze
                // Using a high, unlikely ID for the snooze alarm to avoid collision, or manage it better.
                // For a robust snooze, you might want to store snooze details or use a different mechanism.
                // Here, we just schedule a new one-time alarm.
// TODO:
                val snoozeAlarmItem = AlarmDatabaseItem(
                    id = originalAlarm.id, // Keep original ID for re-fetching details if needed by scheduler, or for label
                    hour = snoozeDateTime.hour,
                    minute = snoozeDateTime.minute,
                    label = "Snoozed: ${originalAlarm.label ?: alarmLabel}",
                    isActive = true,
                    days = "0000000", // Snooze is one-time
                    toneUri = originalAlarm.toneUri, // Use original tone
                    vibrate = originalAlarm.vibrate
                )
                // The AppAlarmScheduler needs to be able to schedule based on absolute time for snooze.
                // For now, let's assume it can. Or, modify AppAlarmScheduler to take millis.
                // This is a simplified snooze. A proper snooze might involve creating a temporary alarm
                // or just directly using AlarmManager.setExactAndAllowWhileIdle with a new PendingIntent.

                Log.d(TAG, "Attempting to schedule snooze for alarm $alarmId at $snoozeDateTime")
                // For simplicity, we create a new alarm that's a copy but with new time
                // This is NOT ideal as it doesn't use the existing `scheduleAlarm` logic correctly for snooze
                // A better way would be to pass the snooze trigger time directly to AlarmManager
                val snoozeIntent = Intent(applicationContext, AlarmReceiver::class.java).apply {
                    action = ACTION_TRIGGER_ALARM
                    putExtra(
                        EXTRA_ALARM_ID,
                        originalAlarm.id
                    ) // Important: use original ID for re-trigger
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    originalAlarm.id + 10000, // Unique request code for snooze PI
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager =
                    getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact snooze alarm, permission missing.")
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        snoozeTimeMillis,
                        snoozePendingIntent
                    )
                    Log.i(TAG, "Snooze scheduled for alarm $alarmId at $snoozeDateTime")
                }
            } else {
                Log.e(TAG, "Original alarm $alarmId not found for snooze.")
            }
            stopSelf(startId) // Stop service after processing snooze
        }
    }


    private fun startRingtone(toneUriString: String?) {
        try {
            val toneUri = if (toneUriString != null) {
                toneUriString.toUri()
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            if (toneUri == null) { // Fallback if default is also null
                Log.w(TAG, "Alarm tone URI is null, and default is null. Cannot play.")
                return
            }

            currentRingtone = RingtoneManager.getRingtone(this, toneUri)
            currentRingtone?.let {
                it.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.isLooping = true
                }
                it.play()
                Log.d(TAG, "Ringtone started for alarmId: $currentAlarmId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ringtone: ", e)
            currentRingtone = null
        }
    }

    private fun showNotification(alarmId: Int, alarmLabel: String) {
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId, // Use alarmId for unique PendingIntent request code for dismiss
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, alarmLabel) // Pass label for snoozed notification
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId + 1, // Unique request code for snooze
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // TODO: For a full-screen alarm, you would create a PendingIntent to launch an Activity here
        // val fullScreenIntent = Intent(this, YourAlarmActivity::class.java).apply {
        // putExtra(EXTRA_ALARM_ID, alarmId)
        // flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // }
        // val fullScreenPendingIntent = PendingIntent.getActivity(this, alarmId + 2,
        // fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Clock")
            .setContentText(alarmLabel)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your alarm icon
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true) // Makes the notification non-swipeable until dismissed by action
            .setAutoCancel(false) // Notification should be explicitly dismissed
            // .setFullScreenIntent(fullScreenPendingIntent, true) // Uncomment for full-screen
            .addAction(0, "Dismiss", dismissPendingIntent)
            .addAction(0, "Snooze (${SNOOZE_DURATION_MINUTES} min)", snoozePendingIntent)
            .build()

        try {
            startForeground(alarmId, notification) // Use alarmId as notification ID
            Log.d(TAG, "Foreground notification shown for alarmId: $alarmId")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service: ${e.message}")
            // Fallback for older Android versions if foreground service type is the issue (rare with mediaPlayback)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(alarmId, notification)
            }
        }
    }


    private fun createNotificationChannel() {
        val name = "Alarm Clock Channel"
        val descriptionText = "Channel for alarm clock notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setSound(null, null) // Sound is handled by the service directly
            enableVibration(true) // Or handle vibration manually with Ringtone/Vibrator
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "AlarmService destroyed.")
        stopCurrentRingtone()
        job.cancel() // Cancel coroutines
        super.onDestroy()
    }
}
