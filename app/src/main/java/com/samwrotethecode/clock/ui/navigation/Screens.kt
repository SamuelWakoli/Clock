package com.samwrotethecode.clock.ui.navigation

sealed class Screens(val route: String) {
    data object AddAlarmScreen : Screens(route = "add_alarm_screen")
    data object AlarmScreen : Screens(route = "alarm_screen")
}