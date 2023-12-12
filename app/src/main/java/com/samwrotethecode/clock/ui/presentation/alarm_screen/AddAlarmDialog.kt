package com.samwrotethecode.clock.ui.presentation.alarm_screen

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    onDismissRequest: () -> Unit,
    viewModel: AlarmViewModel,
) {
    val context = LocalContext.current

    val timePickerState = rememberTimePickerState(
        initialHour = LocalDateTime.now().hour,
        initialMinute = LocalDateTime.now().minute,
        is24Hour = is24Hour ?: DateFormat.is24HourFormat(context)
    )

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(onDismissRequest = { onDismissRequest() }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.AddAlarm,
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
            TimePicker(state = timePickerState, modifier = Modifier.padding(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
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
                                label = null,
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                isActive = true,
                                days = "0000000",
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

@Preview
@Composable
private fun AddAlarmDialogPReview() {
    AddAlarmDialog(
        is24Hour = null,
        onDismissRequest = {},
        viewModel = viewModel(factory = AppViewModelProvider.Factory),
    )
}