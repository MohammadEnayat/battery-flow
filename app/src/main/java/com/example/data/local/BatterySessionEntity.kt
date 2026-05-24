package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ChargingSession

@Entity(tableName = "charging_sessions")
data class BatterySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val startTime: Long,
    val endTime: Long,
    val startLevel: Int,
    val endLevel: Int,
    val maxCurrent: Float,
    val avgVoltage: Float,
    val maxWattage: Float,
    val avgTemperature: Float
) {
    fun toDomain(): ChargingSession = ChargingSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        startLevel = startLevel,
        endLevel = endLevel,
        maxCurrent = maxCurrent,
        avgVoltage = avgVoltage,
        maxWattage = maxWattage,
        avgTemperature = avgTemperature
    )

    companion object {
        fun fromDomain(session: ChargingSession): BatterySessionEntity = BatterySessionEntity(
            id = session.id,
            startTime = session.startTime,
            endTime = session.endTime,
            startLevel = session.startLevel,
            endLevel = session.endLevel,
            maxCurrent = session.maxCurrent,
            avgVoltage = session.avgVoltage,
            maxWattage = session.maxWattage,
            avgTemperature = session.avgTemperature
        )
    }
}
