package com.samwrotethecode.clock.data

import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAlarm(id: Int): Flow<AlarmDatabaseItem>
    fun getAllAlarms(): Flow<List<AlarmDatabaseItem>>
    suspend fun insertAlarm(alarm: AlarmDatabaseItem)
    suspend fun updateAlarm(alarm: AlarmDatabaseItem)
    suspend fun deleteAlarm(alarm: AlarmDatabaseItem)
}

class AlarmOfflineRepository(private val alarmDao: AlarmDao) : AlarmRepository {
    override fun getAlarm(id: Int) = alarmDao.getAlarm(id)

    override fun getAllAlarms(): Flow<List<AlarmDatabaseItem>> = alarmDao.getAllAlarms()

    override suspend fun insertAlarm(alarm: AlarmDatabaseItem) = alarmDao.insertAlarm(alarm)

    override suspend fun updateAlarm(alarm: AlarmDatabaseItem) = alarmDao.updateAlarm(alarm)

    override suspend fun deleteAlarm(alarm: AlarmDatabaseItem) = alarmDao.deleteAlarm(alarm)
}