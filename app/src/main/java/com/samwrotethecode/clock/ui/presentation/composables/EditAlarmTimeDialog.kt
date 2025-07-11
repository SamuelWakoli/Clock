package com.samwrotethecode.clock.ui.presentation.composables

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import com.samwrotethecode.clock.ui.presentation.viewmodels.AlarmViewModel
import com.samwrotethecode.clock.ui.presentation.viewmodels.AppViewModelProvider
import kotlinx.coroutines.launch
import java.time.LocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmTimeDialog(
    alarm: AlarmDatabaseItem,
    is24Hour: Boolean?,
    useKeyboard: Boolean = false,
    onDismissRequest: () -> Unit,
    viewModel: AlarmViewModel,
) {
    val context = LocalContext.current

    val timePickerState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
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
                            viewModel.updateAlarm(
                                alarm.copy(
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                )
                            )
                        }.invokeOnCompletion {
                            Toast.makeText(context, "Alarm updated", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = "Update")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditAlarmTimeDialogPReview() {
    EditAlarmTimeDialog(
        alarm = AlarmDatabaseItem(
            label = null,
            hour = LocalDateTime.now().hour,
            minute = LocalDateTime.now().minute,
            isActive = true,
            days = "0000000",
        ),
        is24Hour = null,
        onDismissRequest = {},
        viewModel = viewModel(factory = AppViewModelProvider.Factory),
    )
}