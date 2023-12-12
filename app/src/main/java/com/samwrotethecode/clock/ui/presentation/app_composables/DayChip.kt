package com.samwrotethecode.clock.ui.presentation.app_composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samwrotethecode.clock.data.AlarmDatabaseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape = CircleShape,
        modifier = Modifier.padding(4.dp),
        border = CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.cardColors(
            containerColor =
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
        ),
        onClick = {
            onClick()
        }
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
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
    )
}

@Preview(showBackground = true)
@Composable
private fun UnselectedDayChipPreview() {
    DayChip(
        label = "S",
        selected = false,
        onClick = {},
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

    Row {
        sampleAlarm.days.forEachIndexed { index, c ->
            DayChip(
                label = daysLabels[index],
                selected = c == '1',
                onClick = {},
            )
        }
    }
}