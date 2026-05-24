package com.example.domain.model

data class BatteryStatus(
    val level: Int = 0,
    val status: Int = 1, // BatteryManager.BATTERY_STATUS_UNKNOWN
    val plugged: Int = 0,
    val voltage: Float = 0f, // in Volts
    val temperature: Float = 0f, // in Celsius
    val currentNow: Float = 0f, // in mA (positive = charging, negative = discharging)
    val wattage: Float = 0f, // in Watts
    val health: Int = 1, // BatteryManager.BATTERY_HEALTH_UNKNOWN
    val chargeTimeRemaining: Long = -1L, // in milliseconds
    val dischargeTimeRemaining: Long = -1L, // in milliseconds
    val capacityMah: Int = 0, // current capacity in mAh
    val maxCapacityMah: Int = 5000, // design/max capacity in mAh
    val dischargeRatePercentPerHour: Float = 0f, // percent per hour
    val technology: String = "Li-ion",
    val androidVersion: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isCharging: Boolean
        get() = status == 2 || status == 5 // CHARGING or FULL

    val healthString: String
        get() = when (health) {
            2 -> "Good"
            3 -> "Overheat"
            4 -> "Dead"
            5 -> "Over Voltage"
            6 -> "Unspecified Failure"
            7 -> "Cold"
            else -> "Unknown"
        }

    val plugTypeString: String
        get() = when (plugged) {
            1 -> "AC Charger"
            2 -> "USB Port"
            4 -> "Wireless"
            else -> "Battery"
        }
}
