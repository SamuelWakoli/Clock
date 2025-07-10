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

class MainActivity : ComponentActivity() {

    private val requestScheduleExactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // No Sdk check needed here as minSdk is 31
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Schedule exact alarm permission granted.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "Schedule exact alarm permission is still not granted.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission is not granted.", Toast.LENGTH_LONG)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClockTheme {
                val navController = rememberNavController()
                var showExactAlarmPermissionRationaleDialog by remember { mutableStateOf(false) }
                var showNotificationPermissionRationaleDialog by remember { mutableStateOf(false) }
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    // Exact Alarm Permission Check - No Sdk check needed here as minSdk is 31
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        showExactAlarmPermissionRationaleDialog = true
                    }

                    // Notification Permission Check (Android 13+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                // Permission is already granted
                            }

                            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                                showNotificationPermissionRationaleDialog = true
                            }

                            else -> {
                                // Directly request the permission
                                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                }

                if (showExactAlarmPermissionRationaleDialog) {
                    ScheduleExactAlarmPermissionDialog(
                        onDismiss = { showExactAlarmPermissionRationaleDialog = false },
                        onConfirm = {
                            showExactAlarmPermissionRationaleDialog = false
                            val intent =
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                            if (intent.resolveActivity(packageManager) != null) {
                                requestScheduleExactAlarmPermissionLauncher.launch(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Could not open settings for exact alarms.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val fallbackIntent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                fallbackIntent.data = Uri.fromParts("package", packageName, null)
                                requestScheduleExactAlarmPermissionLauncher.launch(fallbackIntent)
                            }
                        }
                    )
                }

                if (showNotificationPermissionRationaleDialog) {
                    NotificationPermissionRationaleDialog(
                        onDismiss = { showNotificationPermissionRationaleDialog = false },
                        onConfirm = {
                            showNotificationPermissionRationaleDialog = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Option 1: Guide to settings
                                val intent =
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                                    }
                                if (intent.resolveActivity(packageManager) != null) {
                                    startActivity(intent) // Not using launcher as it's for settings
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Could not open notification settings.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Fallback or direct to app info
                                    val fallbackIntent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    fallbackIntent.data =
                                        Uri.fromParts("package", packageName, null)
                                    startActivity(fallbackIntent)
                                }
                                // Option 2: Re-request (less ideal if rationale was already shown and denied)
                                // requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        navController = navController
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check exact alarm permission - No Sdk check needed here as minSdk is 31
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w("MainActivity", "SCHEDULE_EXACT_ALARM permission not granted onResume.")
        }

        // Re-check notification permission (optional, can be noisy)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("MainActivity", "POST_NOTIFICATIONS permission not granted onResume.")
                // Consider if you want to prompt again or just note it.
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
        title = { Text("Schedule Alarms Permission") },
        text = { Text("This app needs permission to schedule exact alarms to function correctly. Please grant this permission in the app settings.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
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
        title = { Text("Notification Permission Required") },
        text = { Text("This app needs permission to send notifications to alert you about your alarms. Please grant this permission in the app settings.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}
