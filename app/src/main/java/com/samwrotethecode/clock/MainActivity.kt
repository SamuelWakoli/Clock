package com.samwrotethecode.clock

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
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
import androidx.navigation.compose.rememberNavController
import com.samwrotethecode.clock.ui.navigation.NavGraph
import com.samwrotethecode.clock.ui.theme.ClockTheme

class MainActivity : ComponentActivity() {

    private val requestScheduleExactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // User has returned from the settings screen.
            // Re-check the permission. You might want to update UI or state accordingly.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Schedule exact alarm permission granted.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Schedule exact alarm permission is still not granted.", Toast.LENGTH_LONG).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClockTheme {
                val navController = rememberNavController()
                var showPermissionRationaleDialog by remember { mutableStateOf(false) }
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            showPermissionRationaleDialog = true
                        }
                    }
                }

                if (showPermissionRationaleDialog) {
                    ScheduleExactAlarmPermissionDialog(
                        onDismiss = { showPermissionRationaleDialog = false },
                        onConfirm = {
                            showPermissionRationaleDialog = false
                            // Open settings to grant permission
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                            if (intent.resolveActivity(packageManager) != null) {
                                requestScheduleExactAlarmPermissionLauncher.launch(intent)
                            } else {
                                Toast.makeText(context, "Could not open settings.", Toast.LENGTH_SHORT).show()
                                // Fallback or direct to app info
                                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                fallbackIntent.data = Uri.fromParts("package", packageName, null)
                                requestScheduleExactAlarmPermissionLauncher.launch(fallbackIntent)
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

    // It's also a good practice to check when the app resumes
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Potentially re-show rationale or a subtle reminder if permission is crucial
                // For this example, we'll just log it.
                // You might want to update a state variable that your UI observes.
                Log.w("MainActivity", "SCHEDULE_EXACT_ALARM permission not granted onResume.")
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
        title = { Text("Permission Required") },
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

