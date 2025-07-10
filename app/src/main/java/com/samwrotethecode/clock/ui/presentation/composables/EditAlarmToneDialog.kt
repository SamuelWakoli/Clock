package com.samwrotethecode.clock.ui.presentation.composables

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun EditAlarmToneDialog(
    alarm: AlarmDatabaseItem,
    viewModel: AlarmViewModel,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedToneUri by rememberSaveable(alarm.toneUri) { mutableStateOf(alarm.toneUri) }
    var selectedToneName by rememberSaveable(alarm.toneUri) {
        mutableStateOf(
            if (alarm.toneUri != null) {
                RingtoneManager.getRingtone(context, alarm.toneUri.toUri())?.getTitle(context)
                    ?: "Unknown"
            } else {
                "Default"
            }
        )
    }

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
                // If user selects "Silent" or "None", URI might be null
                // RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI could also be an option
                // For simplicity, let's treat null as "Default" here,
                // or you can store null to represent silent.
                selectedToneUri = null // Explicitly set to null for "Silent/None"
                "Default"
            }
        }
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Alarm Tone",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(onClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        putExtra(
                            RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        )
                        // Pass the currently selected tone to the picker
                        selectedToneUri?.let {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it.toUri())
                        }
                    }
                    ringtonePickerLauncher.launch(intent)
                }) {
                    Text("Select Tone: $selectedToneName")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismissRequest() }) {
                        Text(text = "Cancel")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    TextButton(onClick = {
                        coroutineScope.launch {
                            viewModel.updateAlarm(alarm.copy(toneUri = selectedToneUri))
                        }
                        onDismissRequest()
                    }) {
                        Text(text = "Save")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditAlarmToneDialogPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                EditAlarmToneDialog(
                    alarm = AlarmDatabaseItem(
                        id = 0,
                        hour = 12,
                        minute = 30,
                        label = "Preview Alarm",
                        isActive = true,
                        days = "0001000",
                        toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            ?.toString()
                    ),
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    onDismissRequest = {}
                )
            }
        }
    }
}
