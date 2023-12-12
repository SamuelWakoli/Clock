package com.samwrotethecode.clock.ui.presentation.app_composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import com.samwrotethecode.clock.data.AlarmDatabaseItem

@Composable
fun DaysOfWeekText(
    alarm: AlarmDatabaseItem,
    style: TextStyle = TextStyle.Default,
    fontSize: TextUnit = style.fontSize,
    fontWeight: FontWeight? = style.fontWeight,
) {
    val days = mutableListOf<String>()

    alarm.days.forEachIndexed { index, c ->
        if (c == '1')
            when (index) {
                0 -> days + "Sun, "
                1 -> days + "Mon, "
                2 -> days + "Tue, "
                3 -> days + "Wed, "
                4 -> days + "Thu, "
                5 -> days + "Fri, "
                6 -> days + "Sat, "
                else -> {

                }
            }
    }

    return if (days.isNotEmpty()) {
        var lastDay = days.last()
        lastDay = lastDay.trimEnd().trim(',')

        days[days.size - 1] = lastDay

        Text(
            text = days.joinToString(),
            style = style,
            fontSize = fontSize,
            fontWeight = fontWeight,
        )
    } else {
        Text(text = "")
    }
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