package com.samwrotethecode.clock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_table")
data class AlarmDatabaseItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String?,
    val isActive: Boolean,
    val days: String,
    val tone: String? = null,
    val vibrate: Boolean = false,
)
