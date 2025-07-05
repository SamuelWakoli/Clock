package com.samwrotethecode.clock.ui.presentation.app_composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import com.samwrotethecode.clock.ui.presentation.viewmodels.AlarmViewModel
import com.samwrotethecode.clock.ui.presentation.viewmodels.AppViewModelProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditAlarmLabelDialog(
    onDismissRequest: () -> Unit,
    alarm: AlarmDatabaseItem,
    viewModel: AlarmViewModel,
) {
    var label: String by rememberSaveable { mutableStateOf(alarm.label ?: "") }
    var isError: Boolean by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }


    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(text = "Label") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Label,
                            contentDescription = null,
                            tint = if (isError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingIcon = {
                        if (label.isNotEmpty())
                            IconButton(onClick = { label = "" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = null,
                                    tint = if (isError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary,
                                )
                            }
                    },
                    supportingText = {
                        if (isError)
                            Text(text = "Empty label cannot be saved")
                    },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (label.isNotEmpty()) {
                                coroutineScope.launch {
                                    viewModel.updateAlarm(alarm.copy(label = label))
                                }.invokeOnCompletion {
                                    onDismissRequest()
                                }
                            } else {
                                isError = true

                                // After 5 seconds, remove error color
                                coroutineScope.launch {
                                    delay(8000L)
                                }.invokeOnCompletion {
                                    isError = false
                                }
                            }
                        }
                    ),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )

                Spacer(modifier = Modifier.size(8.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = { onDismissRequest() }) {
                        Text(text = "Cancel")
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    TextButton(onClick = {
                        if (label.isNotEmpty()) {
                            coroutineScope.launch {
                                viewModel.updateAlarm(alarm.copy(label = label))
                            }.invokeOnCompletion {
                                onDismissRequest()
                            }
                        } else {
                            isError = true

                            // After 5 seconds, remove error color
                            coroutineScope.launch {
                                delay(8000L)
                            }.invokeOnCompletion {
                                isError = false
                            }
                        }
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
private fun EditAlarmLabelDialogPreview() {
    EditAlarmLabelDialog(
        onDismissRequest = { },
        alarm = AlarmDatabaseItem(
            id = 1,
            label = "Alarm 1",
            hour = 12,
            minute = 0,
            isActive = true,
            days = "0001000"
        ),
        viewModel = viewModel(factory = AppViewModelProvider.Factory),
    )
}