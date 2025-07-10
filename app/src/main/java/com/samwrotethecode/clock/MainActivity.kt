package com.samwrotethecode.clock

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.samwrotethecode.clock.ui.navigation.NavGraph
import com.samwrotethecode.clock.ui.theme.ClockTheme

// Define the permission steps
private enum class PermissionStep {
    IDLE,
    REQUEST_SCHEDULE_EXACT_ALARM,
    REQUEST_POST_NOTIFICATIONS,
    ALL_PERMISSIONS_CHECKED
}

class MainActivity : ComponentActivity() {

    private var currentPermissionStep by mutableStateOf(PermissionStep.IDLE)

    private val requestScheduleExactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Precise alarm permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Precise alarm permission is still not granted.",
                    Toast.LENGTH_LONG
                ).show()
            }
            // Proceed to the next permission step
            currentPermissionStep = PermissionStep.REQUEST_POST_NOTIFICATIONS
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission not granted.", Toast.LENGTH_LONG)
                    .show()
            }
            // All permissions have been requested
            currentPermissionStep = PermissionStep.ALL_PERMISSIONS_CHECKED
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClockTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                var showScheduleExactAlarmRationaleDialog by remember { mutableStateOf(false) }
                var showNotificationPermissionRationaleDialog by remember { mutableStateOf(false) }

                // Start the permission checking process once
                LaunchedEffect(Unit) {
                    if (currentPermissionStep == PermissionStep.IDLE) {
                        currentPermissionStep = PermissionStep.REQUEST_SCHEDULE_EXACT_ALARM
                    }
                }

                LaunchedEffect(currentPermissionStep) {
                    when (currentPermissionStep) {
                        PermissionStep.REQUEST_SCHEDULE_EXACT_ALARM -> {
                            val alarmManager =
                                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            if (!alarmManager.canScheduleExactAlarms()) {
                                showScheduleExactAlarmRationaleDialog = true
                            } else {
                                // Permission already granted, move to next step
                                currentPermissionStep = PermissionStep.REQUEST_POST_NOTIFICATIONS
                            }
                        }

                        PermissionStep.REQUEST_POST_NOTIFICATIONS -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                when {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED -> {
                                        // Permission already granted, all done
                                        currentPermissionStep =
                                            PermissionStep.ALL_PERMISSIONS_CHECKED
                                    }

                                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                                        showNotificationPermissionRationaleDialog = true
                                    }

                                    else -> {
                                        // Directly request the permission
                                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            } else {
                                // Not needed for older SDKs
                                currentPermissionStep = PermissionStep.ALL_PERMISSIONS_CHECKED
                            }
                        }

                        PermissionStep.ALL_PERMISSIONS_CHECKED -> {
                            Log.d("MainActivity", "All permissions checks are done.")
                        }

                        PermissionStep.IDLE -> {
                            // Initial state, waiting for LaunchedEffect(Unit) to kick things off.
                        }
                    }
                }

                // Main app UI - always present, dialogs will overlay
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController = navController)
                }

                if (showScheduleExactAlarmRationaleDialog) {
                    ScheduleExactAlarmPermissionDialog(
                        onDismiss = {
                            showScheduleExactAlarmRationaleDialog = false
                            // User dismissed, move to next permission check
                            currentPermissionStep = PermissionStep.REQUEST_POST_NOTIFICATIONS
                            Toast.makeText(
                                context,
                                "Precise alarm scheduling is recommended for optimal functionality.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onConfirm = {
                            showScheduleExactAlarmRationaleDialog = false
                            val intent =
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                            if (intent.resolveActivity(packageManager) != null) {
                                requestScheduleExactAlarmPermissionLauncher.launch(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Could not open settings for precise alarms.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val fallbackIntent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                fallbackIntent.data = Uri.fromParts("package", packageName, null)
                                requestScheduleExactAlarmPermissionLauncher.launch(fallbackIntent) // Still use launcher to continue flow
                            }
                        }
                    )
                }

                if (showNotificationPermissionRationaleDialog) {
                    NotificationPermissionRationaleDialog(
                        onDismiss = {
                            showNotificationPermissionRationaleDialog = false
                            // User dismissed, all permission checks done for this round
                            currentPermissionStep = PermissionStep.ALL_PERMISSIONS_CHECKED
                            Toast.makeText(
                                context,
                                "Notifications are recommended to ensure you see your alarms.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onConfirm = {
                            showNotificationPermissionRationaleDialog = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                // Should not happen if logic is correct, but as a safeguard:
                                currentPermissionStep = PermissionStep.ALL_PERMISSIONS_CHECKED
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("MainActivity", "onResume check. Current step: $currentPermissionStep")

        // Only try to advance state if permissions are actively being sought
        if (currentPermissionStep != PermissionStep.IDLE && currentPermissionStep != PermissionStep.ALL_PERMISSIONS_CHECKED) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canScheduleExact = alarmManager.canScheduleExactAlarms()

            if (currentPermissionStep == PermissionStep.REQUEST_SCHEDULE_EXACT_ALARM) {
                if (canScheduleExact) {
                    Log.d(
                        "MainActivity",
                        "onResume: Exact alarm permission granted externally. Advancing."
                    )
                    currentPermissionStep = PermissionStep.REQUEST_POST_NOTIFICATIONS
                } else {
                    Log.d("MainActivity", "onResume: Exact alarm permission still not granted.")
                }
            } else if (currentPermissionStep == PermissionStep.REQUEST_POST_NOTIFICATIONS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasNotificationPerm = ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasNotificationPerm) {
                        Log.d(
                            "MainActivity",
                            "onResume: Notification permission granted externally. Advancing."
                        )
                        currentPermissionStep = PermissionStep.ALL_PERMISSIONS_CHECKED
                    } else {
                        Log.d(
                            "MainActivity",
                            "onResume: Notification permission still not granted."
                        )
                    }
                } else { // Below TIRAMISU, notification perm not needed for this step.
                    currentPermissionStep = PermissionStep.ALL_PERMISSIONS_CHECKED
                }
            }
        }
    }
}

@Composable
fun ScheduleExactAlarmPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Precise Alarms") },
        text = { Text("To ensure your alarms ring at the exact time you set, this app needs permission to schedule precise alarms. This is crucial for the core functionality of the alarm clock. Please tap 'Open Settings' to grant this permission.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later")
            }
        }
    )
}

@Composable
fun NotificationPermissionRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allow Notifications") },
        text = { Text("To ensure you don't miss your important alarms, please allow this app to send you notifications. This will allow you to see upcoming alarm alerts, and to dismiss or snooze them directly from the notification. Tap 'Allow' to grant permission.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}
