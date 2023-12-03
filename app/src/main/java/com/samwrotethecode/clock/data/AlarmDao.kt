package com.samwrotethecode.clock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    fun getAlarm(id: Int): Flow<AlarmDatabaseItem>

    @Query("SELECT * FROM alarm_table")
    fun getAllAlarms(): Flow<List<AlarmDatabaseItem>>

    @Insert(entity = AlarmDatabaseItem::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlarm(alarm: AlarmDatabaseItem)

    @Update(entity = AlarmDatabaseItem::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateAlarm(alarm: AlarmDatabaseItem)

    @Delete(entity = AlarmDatabaseItem::class)
    suspend fun deleteAlarm(alarm: AlarmDatabaseItem)

}
