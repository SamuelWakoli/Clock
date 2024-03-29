package com.samwrotethecode.clock.ui.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samwrotethecode.clock.alarm_scheduler.AlarmScheduler
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import com.samwrotethecode.clock.data.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class AlarmsUiState(
    val alarms: List<AlarmDatabaseItem>,
)

data class AlarmScreenUiState(
    val currentAlarm: AlarmDatabaseItem? = null,
)

class AlarmViewModel(
    val alarmRepository: AlarmRepository,
    val alarmScheduler: AlarmScheduler,
) :
    ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    val alarmsUiState: StateFlow<AlarmsUiState?> =
        alarmRepository.getAllAlarms().map { AlarmsUiState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = null
        )

    private var _uiState = MutableStateFlow(AlarmScreenUiState())
    val uiState = _uiState.asStateFlow()

    //TODO: NOTE THAT alarm.days WILL BE ALWAYS CHECKED IN BROADCAST RECEIVER
    suspend fun addAlarm(alarm: AlarmDatabaseItem) {
        alarmRepository.insertAlarm(alarm)
        // TODO: Schedule alarm here
        alarmScheduler.scheduleAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: AlarmDatabaseItem) {
        alarmRepository.updateAlarm(alarm)
        // TODO: un-schedule and reschedule alarm
        if (alarm.isActive) {
            alarmScheduler.cancelAlarm(alarm)
            alarmScheduler.scheduleAlarm(alarm)
        } else {
            alarmScheduler.cancelAlarm(alarm)
        }
    }

    suspend fun deleteAlarm(alarm: AlarmDatabaseItem) {
        alarmRepository.deleteAlarm(alarm)
        // TODO: un-schedule alarm
        alarmScheduler.cancelAlarm(alarm)
    }

    fun setCurrentAlarm(alarm: AlarmDatabaseItem?) = _uiState.update {
        it.copy(currentAlarm = alarm)
    }
}