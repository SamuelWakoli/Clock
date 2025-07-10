package com.samwrotethecode.clock.ui.presentation.alarm_screen

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import com.samwrotethecode.clock.ui.presentation.viewmodels.AlarmViewModel
import com.samwrotethecode.clock.ui.presentation.viewmodels.AppViewModelProvider
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    is24Hour: Boolean?,
    useKeyboard: Boolean = false,
    onDismissRequest: () -> Unit,
    viewModel: AlarmViewModel,
) {
    val context = LocalContext.current

    var selectedToneUri by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedToneName by rememberSaveable { mutableStateOf<String?>(null) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            selectedToneUri = uri?.toString()
            selectedToneName = if (uri != null) {
                RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: "Unknown"
            } else {
                "Default" // Should not happen if RESULT_OK and uri is null, but as a fallback
            }
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = LocalDateTime.now().hour,
        initialMinute = LocalDateTime.now().minute,
        is24Hour = is24Hour ?: DateFormat.is24HourFormat(context)
    )

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.padding(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                if (useKeyboard) TimeInput(state = timePickerState)
                else TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        putExtra(
                            RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        )
                        selectedToneUri?.let {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it.toUri())
                        }
                    }
                    ringtonePickerLauncher.launch(intent)
                }) {
                    if (selectedToneUri == null) Text("Select Tone") else Text("Select Tone: $selectedToneName")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismissRequest() }) {
                        Text(text = "Cancel")
                    }
                    TextButton(onClick = {
                        onDismissRequest()
                        coroutineScope.launch {
                            viewModel.addAlarm(
                                alarm = AlarmDatabaseItem(
                                    label = null, // You might want to add a TextField for the label
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                    isActive = false,
                                    days = "0000000", // Add UI for selecting days
                                    toneUri = selectedToneUri,
                                    vibrate = false // Add UI for vibrate toggle
                                )
                            )
                        }.invokeOnCompletion {
                            Toast.makeText(context, "Alarm added", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = "Save")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AddAlarmDialogPReview() {
    AddAlarmDialog(
        is24Hour = null,
        onDismissRequest = {},
        viewModel = viewModel(factory = AppViewModelProvider.Factory),
    )
}