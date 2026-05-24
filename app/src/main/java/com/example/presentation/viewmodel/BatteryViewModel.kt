package com.example.presentation.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BatteryStatus
import com.example.domain.model.BatteryTip
import com.example.domain.model.ChargingSession
import com.example.domain.repository.BatteryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class BatteryViewModel(
    private val app: Application,
    private val repository: BatteryRepository
) : AndroidViewModel(app) {

    private val _batteryState = MutableStateFlow(BatteryStatus())
    val batteryState: StateFlow<BatteryStatus> = _batteryState.asStateFlow()

    private var activeSessionStartLevel: Int = -1
    private var activeSessionStartTime: Long = -1L
    private var voltageSum = 0f
    private var voltageCount = 0
    private var tempSum = 0f
    private var tempCount = 0
    private var peakCurrentSum = 0f
    private var peakWattageSum = 0f
    private var isTrackedActiveSession = false

    private val _isChargingSessionActive = MutableStateFlow(false)
    val isChargingSessionActive = _isChargingSessionActive.asStateFlow()

    private var alertedFullForCurrentSession = false

    val chargingHistory: StateFlow<List<ChargingSession>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _batteryTips = MutableStateFlow<List<BatteryTip>>(emptyList())
    val batteryTips: StateFlow<List<BatteryTip>> = _batteryTips.asStateFlow()

    private val _isLoadingTips = MutableStateFlow(false)
    val isLoadingTips = _isLoadingTips.asStateFlow()

    private val batteryManager = app.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private fun getSystemBatteryCapacity(context: Context): Double {
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfileConstructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfileInstance = powerProfileConstructor.newInstance(context)
            val getAveragePowerMethod = powerProfileClass.getMethod("getAveragePower", String::class.java)
            val capacity = getAveragePowerMethod.invoke(powerProfileInstance, "battery.capacity") as Double
            if (capacity > 0) return capacity
        } catch (_: Exception) {}
        return 5000.0 // Realistic standard fallback
    }

    private fun parseBatteryIntent(context: Context, intent: Intent): BatteryStatus {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryLevel = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val voltage = voltageMv / 1000f // Volts

        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperature = tempTenths / 10f // Celsius

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)

        var currentMicroAmps = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        // Check for common scaling differences in microamps/milliamps
        if (abs(currentMicroAmps) > 1000000) {
            currentMicroAmps /= 1000
        } else if (abs(currentMicroAmps) > 100000) {
            currentMicroAmps /= 1000
        }
        val currentNow = currentMicroAmps.toFloat() // mA

        val wattage = voltage * (currentNow / 1000f)

        val maxCap = getSystemBatteryCapacity(context).toInt()
        val chargeCounterMicroAh = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        var capacityMah = if (chargeCounterMicroAh > 0) {
            chargeCounterMicroAh / 1000
        } else {
            ((batteryLevel / 100f) * maxCap).toInt()
        }
        if (capacityMah !in 1..maxCap) {
            capacityMah = ((batteryLevel / 100f) * maxCap).toInt()
        }

        val timeRemaining = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            batteryManager.computeChargeTimeRemaining()
        } else {
            -1L
        }

        var dischargeTimeRemaining = -1L
        var dischargeRatePercentPerHour = 0f

        val isChargingValue = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        if (!isChargingValue) {
            val drainCurrentMa = abs(currentNow)
            if (drainCurrentMa > 10f) {
                dischargeRatePercentPerHour = (drainCurrentMa / maxCap.toFloat()) * 100f
                val hoursRemaining = capacityMah.toFloat() / drainCurrentMa
                dischargeTimeRemaining = (hoursRemaining * 3600000).toLong()
            } else {
                val fallbackDrain = 250f
                dischargeRatePercentPerHour = (fallbackDrain / maxCap.toFloat()) * 100f
                val hoursRemaining = capacityMah.toFloat() / fallbackDrain
                dischargeTimeRemaining = (hoursRemaining * 3600000).toLong()
            }
        }

        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        return BatteryStatus(
            level = batteryLevel,
            status = status,
            plugged = plugged,
            voltage = voltage,
            temperature = temperature,
            currentNow = currentNow,
            wattage = wattage,
            health = health,
            chargeTimeRemaining = timeRemaining,
            dischargeTimeRemaining = dischargeTimeRemaining,
            capacityMah = capacityMah,
            maxCapacityMah = maxCap,
            dischargeRatePercentPerHour = dischargeRatePercentPerHour,
            technology = technology,
            androidVersion = Build.VERSION.RELEASE,
            manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            model = Build.MODEL,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun createBatteryStatusFlow(): Flow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(parseBatteryIntent(context, intent))
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        app.registerReceiver(receiver, filter)

        // Try to push an initial state immediately using standard Android sticky intent behavior
        val stickyIntent = app.registerReceiver(null, filter)
        if (stickyIntent != null) {
            trySend(parseBatteryIntent(app, stickyIntent))
        }

        awaitClose {
            try {
                app.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    init {
        viewModelScope.launch {
            createBatteryStatusFlow().collect { statusUpdate ->
                _batteryState.value = statusUpdate
                handleBatteryUpdate(statusUpdate)
            }
        }
        loadTips()
    }

    private fun handleBatteryUpdate(status: BatteryStatus) {
        if (status.isCharging) {
            if (!isTrackedActiveSession) {
                activeSessionStartLevel = status.level
                activeSessionStartTime = System.currentTimeMillis()
                voltageSum = 0f
                voltageCount = 0
                tempSum = 0f
                tempCount = 0
                peakCurrentSum = 0f
                peakWattageSum = 0f
                isTrackedActiveSession = true
                _isChargingSessionActive.value = true
                alertedFullForCurrentSession = false
            }

            voltageSum += status.voltage
            voltageCount++
            tempSum += status.temperature
            tempCount++
            
            val absCurrent = abs(status.currentNow)
            if (absCurrent > peakCurrentSum) {
                peakCurrentSum = absCurrent
            }
            val absWattage = abs(status.wattage)
            if (absWattage > peakWattageSum) {
                peakWattageSum = absWattage
            }

            if (status.level >= 100 && !alertedFullForCurrentSession) {
                sendFullyChargedNotification()
                alertedFullForCurrentSession = true
            }

        } else {
            if (isTrackedActiveSession) {
                val endTime = System.currentTimeMillis()
                val finalLevel = status.level
                
                if (endTime - activeSessionStartTime > 3000) { // At least 3 seconds
                    val avgVoltage = if (voltageCount > 0) (voltageSum / voltageCount) else status.voltage
                    val avgTemp = if (tempCount > 0) (tempSum / tempCount) else status.temperature

                    val finishedSession = ChargingSession(
                        startTime = activeSessionStartTime,
                        endTime = endTime,
                        startLevel = activeSessionStartLevel,
                        endLevel = finalLevel,
                        maxCurrent = peakCurrentSum,
                        avgVoltage = avgVoltage,
                        maxWattage = peakWattageSum,
                        avgTemperature = avgTemp
                    )

                    viewModelScope.launch(Dispatchers.IO) {
                        repository.saveSession(finishedSession)
                    }
                }
                
                isTrackedActiveSession = false
                _isChargingSessionActive.value = false
            }
        }
    }

    private fun sendFullyChargedNotification() {
        val notificationManager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val launchIntent = app.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                app, 
                0, 
                launchIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val notification = NotificationCompat.Builder(app, "battery_full_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Battery Fully Charged! 🎉")
            .setContentText("Your device is 100% full. Unplug to protect battery longevity.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }
            .build()

        notificationManager.notify(101, notification)
    }

    fun loadTips() {
        viewModelScope.launch {
            _isLoadingTips.value = true
            val tips = repository.fetchBatteryTips()
            _batteryTips.value = tips
            _isLoadingTips.value = false
        }
    }

    fun deleteSession(session: ChargingSession) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSession(session.id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllSessions()
        }
    }

}
