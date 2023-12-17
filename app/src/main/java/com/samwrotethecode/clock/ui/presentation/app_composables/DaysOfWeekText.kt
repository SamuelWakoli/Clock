package com.samwrotethecode.clock.ui.presentation.app_composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import java.time.LocalDateTime

@Composable
fun DaysOfWeekText(
    alarm: AlarmDatabaseItem,
    style: TextStyle = TextStyle.Default,
    fontSize: TextUnit = style.fontSize,
    fontWeight: FontWeight? = style.fontWeight,
) {
    val currentTimeSnapshot = LocalDateTime.now()
    val daysText: String
    val days = mutableListOf<String>()

    if (alarm.days == "0000000" && !alarm.isActive) daysText = "Not scheduled"
    else if (alarm.days != "0000000" && !alarm.isActive) daysText = "Not scheduled"
    else if (alarm.days == "1111111") daysText = "Repeats daily"
    else if (alarm.days == "0111110") daysText = "Repeats on weekdays"
    else if (alarm.days == "1000001") daysText = "Repeats on weekends"
    else if (alarm.days == "0000000" &&
        alarm.hour < currentTimeSnapshot.hour &&
        alarm.minute < currentTimeSnapshot.minute
    ) daysText = "Tomorrow"
    else if (alarm.days == "0000000") daysText = "Today"
    else {
        alarm.days.forEachIndexed { index, c ->
            if (c == '1')
                when (index) {
                    0 -> days += "Sun, "
                    1 -> days += "Mon, "
                    2 -> days += "Tue, "
                    3 -> days += "Wed, "
                    4 -> days += "Thu, "
                    5 -> days += "Fri, "
                    6 -> days += "Sat, "
                }
        }

        if (days.isEmpty()) {
            daysText = "Not scheduled"
        } else {
            var lastDay = days.last()
            lastDay = lastDay.trimEnd().trim(',')
            days[days.size - 1] = lastDay

            daysText = days.joinToString(separator = "")
        }
    }

    Text(
        text = daysText.trimEnd(','),
        fontSize = fontSize,
        fontWeight = fontWeight,
        style = style,
    )
}


@Preview
@Composable
private fun DaysOfWeekTextPreview() {
    DaysOfWeekText(
        alarm = AlarmDatabaseItem(
            id = 1,
            hour = 12,
            minute = 30,
            label = null,
            isActive = true,
            days = "0110110"
        )
    )
}