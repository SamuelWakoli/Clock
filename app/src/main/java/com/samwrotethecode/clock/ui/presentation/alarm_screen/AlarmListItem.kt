package com.samwrotethecode.clock.ui.presentation.alarm_screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
fun AlarmListItem(alarm: AlarmDatabaseItem, viewModel: AlarmViewModel, is24HourFormat: Boolean) {
    var expandedState by rememberSaveable { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f,
        label = "",
    )

    var isToggled by rememberSaveable {
        mutableStateOf(alarm.isActive)
    }

    val coroutineScope = rememberCoroutineScope()

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
            .padding(vertical = 4.dp, horizontal = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier
                .clickable { /*TODO*/ }
                .padding(16.dp)
                .weight(9f)) {
                Icon(
                    imageVector = if (alarm.label == null) Icons.Outlined.NewLabel else Icons.Outlined.Label,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(text = alarm.label ?: "Add label")
            }
            Icon(
                imageVector = if (expandedState) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .rotate(rotationState)
            )
        }
        Row {
            Text(text = buildAnnotatedString {
                val timeExtension = if (is24HourFormat) "" else if (alarm.hour < 12) "AM" else "PM"
                withStyle(
                    style = SpanStyle(
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        color = MaterialTheme.colorScheme.primary,
                    )
                ) {
                    append("${alarm.hour}:${alarm.minute}")
                }
                append(timeExtension)
            })
            Switch(checked = isToggled, onCheckedChange = {
                isToggled = it
                coroutineScope.launch {
                    viewModel.updateAlarm(
                        alarm.copy(isActive = it)
                    )
                }
            })

        }
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
    )
}