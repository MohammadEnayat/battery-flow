package com.example.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.viewmodel.BatteryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BatteryViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val batteryState by viewModel.batteryState.collectAsStateWithLifecycle()
    val isSessionActive by viewModel.isChargingSessionActive.collectAsStateWithLifecycle()
    val historyLog by viewModel.chargingHistory.collectAsStateWithLifecycle()
    val tips by viewModel.batteryTips.collectAsStateWithLifecycle()
    val isLoadingTips by viewModel.isLoadingTips.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "POWER DASHBOARD"
                            1 -> "CHARGING RECORDS"
                            else -> "BATTERY INFOCENTER"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = sp(1.2f)
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = "Dashboard") },
                    label = { Text("Power", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Insights") },
                    label = { Text("Insights", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    batteryStatus = batteryState,
                    isSessionActive = isSessionActive
                )
                1 -> HistoryScreen(
                    sessions = historyLog,
                    onDeleteSession = { viewModel.deleteSession(it) },
                    onClearAll = { viewModel.clearHistory() }
                )
                2 -> TipsScreen(
                    tips = tips,
                    isLoading = isLoadingTips,
                    onRefresh = { viewModel.loadTips() }
                )
            }
        }
    }
}

private fun sp(value: Float): androidx.compose.ui.unit.TextUnit {
    return value.sp
}
