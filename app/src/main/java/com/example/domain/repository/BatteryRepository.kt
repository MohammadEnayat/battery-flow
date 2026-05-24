package com.example.domain.repository

import com.example.domain.model.BatteryTip
import com.example.domain.model.ChargingSession
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    fun getAllSessions(): Flow<List<ChargingSession>>
    suspend fun saveSession(session: ChargingSession): Long
    suspend fun deleteSession(id: Long)
    suspend fun clearAllSessions()
    suspend fun fetchBatteryTips(): List<BatteryTip>
}
