package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BatterySessionDao {
    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<BatterySessionEntity>>

    @Upsert
    suspend fun insertSession(session: BatterySessionEntity): Long

    @Query("DELETE FROM charging_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM charging_sessions")
    suspend fun deleteAllSessions()
}
