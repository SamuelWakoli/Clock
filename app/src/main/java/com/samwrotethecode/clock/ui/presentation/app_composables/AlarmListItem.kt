package com.samwrotethecode.clock.ui.presentation.app_composables

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import com.samwrotethecode.clock.ui.presentation.viewmodels.AlarmViewModel
import com.samwrotethecode.clock.ui.presentation.viewmodels.AppViewModelProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListItem(
    alarm: AlarmDatabaseItem,
    viewModel: AlarmViewModel,
    is24HourFormat: Boolean,
    useKeyboard: Boolean,
) {
    var expandedState by rememberSaveable { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f,
        label = "",
    )

    var isToggled by rememberSaveable {
        mutableStateOf(alarm.isActive)
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showEditLabelDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showEditTimeDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showEditAlarmToneDialog by rememberSaveable {
        mutableStateOf(false)
    }

    Card(
        onClick = {
            expandedState = !expandedState
            if (expandedState) viewModel.setCurrentAlarm(alarm)
            else viewModel.setCurrentAlarm(null)
        },
        modifier = Modifier
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300, easing = EaseOutSine
                )
            )
            .widthIn(min = 400.dp, max = 600.dp)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(9f)
            ) {
                if (expandedState || alarm.label != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditLabelDialog = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (alarm.label == null) Icons.Outlined.NewLabel else Icons.Outlined.Label,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(
                            text = alarm.label ?: "Add label",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                if (alarm.label == null) Spacer(modifier = Modifier.size(8.dp))
                Text(
                    modifier = if (expandedState) Modifier
                        .clickable {
                            showEditTimeDialog = true

                        }
                        .padding(horizontal = 16.dp)
                    else Modifier.padding(horizontal = 16.dp),
                    text = buildAnnotatedString {
                        val timeExtension =
                            if (is24HourFormat) "" else if (alarm.hour < 12) "AM" else "PM"
                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.displayLarge.fontSize,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        ) {
                            val hour: String =
                                if (!is24HourFormat && alarm.hour > 12) (alarm.hour - 12).toString()
                                else if (!is24HourFormat && alarm.hour == 0) 12.toString()
                                else alarm.hour.toString()

                            val minute: String =
                                if (alarm.minute < 10) "0${alarm.minute}" else alarm.minute.toString()
                            append("${if (hour.length != 2) ("0$hour") else hour}:$minute")
                        }
                        append(" $timeExtension")
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                )
            }


            IconButton(
                onClick = {
                    expandedState = !expandedState
                    if (expandedState) viewModel.setCurrentAlarm(alarm)
                    else viewModel.setCurrentAlarm(null)
                },
                modifier = Modifier
                    .weight(2f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Icon(
                    // Since we are using rotationState, KeyboardArrowDown will be rotated by 180 degrees
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotationState)
                )
            }
        }


        ListItem(modifier = Modifier.height(48.dp), colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ), headlineContent = { DaysOfWeekText(alarm = alarm) }, trailingContent = {
            Switch(checked = isToggled, onCheckedChange = {
                isToggled = it
                coroutineScope.launch {
                    viewModel.updateAlarm(
                        alarm.copy(isActive = it)
                    )
                }
            })
        })


        // This content will only be visible if expandedState is true
        if (expandedState) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(text = "Schedule", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    alarm.days.forEachIndexed { index, c ->
                        DayChip(label = when (index) {
                            0 -> "S"
                            1 -> "M"
                            2 -> "T"
                            3 -> "W"
                            4 -> "T"
                            5 -> "F"
                            6 -> "S"
                            else -> ""
                        }, dayIndex = index, selected = c == '1', onClick = {
                            val newDays = alarm.days.toCharArray()
                            newDays[index] = if (it) '1' else '0'

                            coroutineScope.launch {
                                viewModel.updateAlarm(
                                    alarm.copy(
                                        days = newDays.concatToString()
                                    )
                                )
                            }
                        })
                    }
                }
            }
            ListItem(
                modifier = Modifier.clickable { showEditAlarmToneDialog = true },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(text = "Default (Cesium)")
                },
            )

            ListItem(
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        viewModel.updateAlarm(alarm.copy(vibrate = !alarm.vibrate))
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ), leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Vibration,
                        contentDescription = null,
                    )
                }, headlineContent = {
                    Text(text = "Vibrate")
                }, trailingContent = {
                    CircularCheckbox(checked = alarm.vibrate) {

                    }
                })
            ListItem(
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        viewModel.deleteAlarm(alarm)
                    }.invokeOnCompletion {
                        expandedState = false
                        viewModel.setCurrentAlarm(null)
                        Toast.makeText(
                            context,
                            "Alarm deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(text = "Delete")
                },
            )
        }
    }

    if (showEditLabelDialog) {
        EditAlarmLabelDialog(
            onDismissRequest = { showEditLabelDialog = false },
            alarm = alarm,
            viewModel = viewModel
        )
    }

    if (showEditTimeDialog) {
        EditAlarmTimeDialog(
            alarm = alarm,
            is24Hour = is24HourFormat,
            useKeyboard = useKeyboard,
            onDismissRequest = {
                showEditTimeDialog = false
            },
            viewModel = viewModel,
        )
    }

    if (showEditAlarmToneDialog) {
        EditAlarmToneDialog(
            onDismissRequest = {
                showEditAlarmToneDialog = false
            })
    }
}

@Preview
@Composable
private fun AlarmListItemPreview() {
    AlarmListItem(
        alarm = AlarmDatabaseItem(
            id = 0,
            hour = 12,
            minute = 30,
            label = "Practice Compose",
            isActive = true,
            days = "0001000"
        ),
        viewModel = viewModel(factory = AppViewModelProvider.Factory),
        is24HourFormat = false,
        useKeyboard = false,
    )
}