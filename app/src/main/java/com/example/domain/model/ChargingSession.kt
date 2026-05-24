package com.example.domain.model

data class ChargingSession(
    val id: Long = 0L,
    val startTime: Long,
    val endTime: Long,
    val startLevel: Int,
    val endLevel: Int,
    val maxCurrent: Float, // in mA
    val avgVoltage: Float, // in V
    val maxWattage: Float, // in W
    val avgTemperature: Float // in Celsius
)
