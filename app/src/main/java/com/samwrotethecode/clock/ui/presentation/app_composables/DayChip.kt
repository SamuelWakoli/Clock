package com.samwrotethecode.clock.ui.presentation.app_composables

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samwrotethecode.clock.data.AlarmDatabaseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayChip(
    label: String,
    dayIndex: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        shape = CircleShape,
        modifier = Modifier.padding(2.dp),
        border = CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.cardColors(
            containerColor =
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
        ),
        onClick = {
            val dayFullName = when (dayIndex) {
                0 -> "Sunday"
                1 -> "Monday"
                2 -> "Tuesday"
                3 -> "Wednesday"
                4 -> "Thursday"
                5 -> "Friday"
                6 -> "Saturday"
                else -> "Unknown"
            }

            onClick()

            if (selected)
                Toast.makeText(context, "Alarm will repeat on $dayFullName", Toast.LENGTH_SHORT)
                    .show()
        }
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedDayChipPreview() {
    DayChip(
        label = "S",
        selected = true,
        onClick = {},
        dayIndex = 0,
    )
}

@Preview(showBackground = true)
@Composable
private fun UnselectedDayChipPreview() {
    DayChip(
        label = "S",
        selected = false,
        onClick = {},
        dayIndex = 0,
    )
}

@Preview
@Composable
private fun DayChipsRowPreview() {
    val sampleAlarm = AlarmDatabaseItem(
        id = 1,
        label = "Sample Alarm",
        hour = 12,
        minute = 0,
        isActive = true,
        days = "0110010",
    )

    val daysLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    ListItem(
        headlineContent = {
            Row {
                sampleAlarm.days.forEachIndexed { index, c ->
                    DayChip(
                        label = daysLabels[index],
                        dayIndex = index,
                        selected = c == '1',
                        onClick = {},
                    )
                }
            }
        },
    )
}