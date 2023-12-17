package com.samwrotethecode.clock.ui.presentation.viewmodels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samwrotethecode.clock.ui.AlarmApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AlarmViewModel(
                alarmRepository = alarmApp().container.alarmRepository,
                alarmScheduler = alarmApp().container.alarmScheduler,
            )
        }

        //Add other initializers here
    }
}

fun CreationExtras.alarmApp(): AlarmApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlarmApp)