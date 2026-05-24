package com.example.presentation.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BatteryStatus
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    batteryStatus: BatteryStatus,
    isSessionActive: Boolean,
    modifier: Modifier = Modifier
) {
    var hasNotificationPermission by remember { mutableStateOf(true) }

    // Request notification permission for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Notification Permission Banner for safety
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                     colors = CardDefaults.cardColors(
                         containerColor = MaterialTheme.colorScheme.errorContainer
                     ),
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(bottom = 12.dp),
                     shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification alert",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Allow Notifications",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Get alerted in real-time when the battery is fully charged.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onError
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.heightIn(max = 32.dp)
                        ) {
                            Text("Enable", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Real-time circular Battery Status Gauge
            BatteryCircularGauge(
                level = batteryStatus.level,
                isCharging = batteryStatus.isCharging,
                modifier = Modifier
                    .size(220.dp)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Charge Remaining estimation banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (batteryStatus.isCharging) "Charging via ${batteryStatus.plugTypeString}" else "Discharging (On Battery)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (batteryStatus.isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val timeText = if (batteryStatus.isCharging) {
                        if (batteryStatus.chargeTimeRemaining > 0) {
                            val mins = (batteryStatus.chargeTimeRemaining / 60000) % 60
                            val hrs = batteryStatus.chargeTimeRemaining / 3600000
                            if (hrs > 0) "${hrs}h ${mins}m left until Full" else "${mins}m left until Full"
                        } else {
                            // Rough estimation mechanism if system doesn't report it immediately
                            val remainingLevel = 100 - batteryStatus.level
                            if (remainingLevel <= 0) {
                                "Battery Fully Charged"
                            } else if (abs(batteryStatus.currentNow) > 50) {
                                val estHours = (remainingLevel * 40f) / abs(batteryStatus.currentNow) // 4000mAh base approximation
                                val totalMins = (estHours * 60).toLong()
                                if (totalMins > 0) {
                                    val hrs = totalMins / 60
                                    val mins = totalMins % 60
                                    if (hrs > 0) "~${hrs}h ${mins}m left until Full" else "~${mins}m left until Full"
                                } else "Calculating time remaining..."
                            } else {
                                "Calculating time remaining..."
                            }
                        }
                    } else {
                        if (batteryStatus.dischargeTimeRemaining > 0) {
                            val mins = (batteryStatus.dischargeTimeRemaining / 60000) % 60
                            val hrs = batteryStatus.dischargeTimeRemaining / 3600000
                            val rateText = String.format(LocalLocale.current.platformLocale,"%.1f%%", batteryStatus.dischargeRatePercentPerHour)
                            "Discharging at $rateText per hour\nEstimated backup time: ${hrs}hrs ${mins}mins"
                        } else {
                            "Discharging\nCalculating backup time..."
                        }
                    }
                    
                    Text(
                        text = timeText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Linear capacity utilization bar matching mockup
                    val capacityFraction = if (batteryStatus.maxCapacityMah > 0) {
                        batteryStatus.capacityMah.toFloat() / batteryStatus.maxCapacityMah.toFloat()
                    } else 0.8f

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Battery Capacity",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${batteryStatus.capacityMah} mAh / ${batteryStatus.maxCapacityMah} mAh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        LinearProgressIndicator(
                            progress = { capacityFraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    if (isSessionActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Actively recording charging session stats...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Metrics Grid (Wattage, Voltage, Temp, Health, etc.)
            Text(
                text = "Real-time Power Analytics",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp, start = 4.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Wattage
                item {
                    MetricCard(
                        title = "Power Wattage",
                        value = String.format(LocalLocale.current.platformLocale,"%.2f W", abs(batteryStatus.wattage)),
                        icon = Icons.Default.Bolt,
                        color = MaterialTheme.colorScheme.primary,
                        testTag = "metric_wattage"
                    )
                }

                // Current Now
                item {
                    val currentLabel = if (batteryStatus.currentNow >= 0) "In-flow Current" else "Out-flow Current"
                    MetricCard(
                        title = currentLabel,
                        value = "${abs(batteryStatus.currentNow).toInt()} mA",
                        icon = Icons.Default.Speed,
                        color = if (batteryStatus.currentNow >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        testTag = "metric_current"
                    )
                }

                // Voltage
                item {
                    MetricCard(
                        title = "Voltage Value",
                        value = String.format(LocalLocale.current.platformLocale,"%.3f V", batteryStatus.voltage),
                        icon = Icons.Default.FlashOn,
                        color = MaterialTheme.colorScheme.tertiary,
                        testTag = "metric_voltage"
                    )
                }

                // Temperature
                item {
                    val tempColor = if (batteryStatus.temperature > 37f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    MetricCard(
                        title = "Temperature",
                        value = "${batteryStatus.temperature} °C",
                        icon = Icons.Default.Thermostat,
                        color = tempColor,
                        testTag = "metric_temperature"
                    )
                }

                // Health Insights
                item {
                    val healthColor = if (batteryStatus.health == 2) Color(0xFF4CAF50) else Color(0xFFFFB300)
                    MetricCard(
                        title = "Battery Health",
                        value = batteryStatus.healthString,
                        icon = Icons.Default.FavoriteBorder,
                        color = healthColor,
                        testTag = "metric_health"
                    )
                }

                // Plug Type / Source
                item {
                    MetricCard(
                        title = "Power Source",
                        value = batteryStatus.plugTypeString,
                        icon = Icons.Default.Power,
                        color = MaterialTheme.colorScheme.outline,
                        testTag = "metric_source"
                    )
                }

                // Max Capacity Card
                item {
                    MetricCard(
                        title = "Max Capacity",
                        value = "${batteryStatus.maxCapacityMah} mAh",
                        icon = Icons.Default.BatteryChargingFull,
                        color = MaterialTheme.colorScheme.primary,
                        testTag = "metric_max_capacity"
                    )
                }

                // Technology Card
                item {
                    MetricCard(
                        title = "Technology",
                        value = batteryStatus.technology,
                        icon = Icons.Default.Memory,
                        color = MaterialTheme.colorScheme.secondary,
                        testTag = "metric_technology"
                    )
                }

                // Android Version Card
                item {
                    MetricCard(
                        title = "Android Version",
                        value = batteryStatus.androidVersion,
                        icon = Icons.Default.PhoneAndroid,
                        color = MaterialTheme.colorScheme.tertiary,
                        testTag = "metric_android_version"
                    )
                }

                // Manufacturer Card
                item {
                    MetricCard(
                        title = "Manufacturer",
                        value = batteryStatus.manufacturer,
                        icon = Icons.Default.Domain,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        testTag = "metric_manufacturer"
                    )
                }

                // Model Card
                item {
                    MetricCard(
                        title = "Model",
                        value = batteryStatus.model,
                        icon = Icons.Default.Smartphone,
                        color = MaterialTheme.colorScheme.outline,
                        testTag = "metric_model"
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryCircularGauge(
    level: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = level / 100f

    // Animated colors and pulse rings for active charging feedback (premium layout)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by if (isCharging) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        remember { mutableFloatStateOf(0.4f) }
    }

    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.secondary
    val colorBackground = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 12.dp.toPx()

            // Draw clean background ring track
            drawCircle(
                color = colorBackground,
                radius = radius,
                center = center,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dynamic Gradient for charging indicator
            val gradient = Brush.sweepGradient(
                colors = listOf(colorPrimary, colorSecondary, colorPrimary),
                center = center
            )

            // Draw progress arc
            drawArc(
                brush = if (isCharging) gradient else Brush.linearGradient(listOf(colorPrimary, colorSecondary)),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )

            // Pulsing charging glow ring
            if (isCharging) {
                drawCircle(
                    color = colorPrimary.copy(alpha = pulseAlpha * 0.15f),
                    radius = radius + 10.dp.toPx(),
                    center = center,
                    style = Stroke(width = 10.dp.toPx())
                )
            }
        }

        // Inner Percentage text labels
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = null,
                tint = if (isCharging) colorPrimary else colorSecondary,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("battery_icon_stat")
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$level%",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 44.sp,
                modifier = Modifier.testTag("battery_percentage_text")
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = if (isCharging) "CHARGING" else "DISCHARGING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCharging) colorPrimary else colorSecondary.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
