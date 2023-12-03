package com.samwrotethecode.clock

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samwrotethecode.clock.ui.AlarmApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // TODO: Add initializers here

    }
}

fun CreationExtras.alarmApp(): AlarmApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlarmApp)