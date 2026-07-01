package com.segnities007.client.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.segnities007.client.ui.screen.DashboardScreen
import com.segnities007.client.ui.screen.DetectionListScreen
import com.segnities007.client.ui.viewmodel.DashboardViewModel
import com.segnities007.client.ui.viewmodel.DetectionViewModel

private enum class AppDestination {
    HISTORY,
    DASHBOARD,
}

@Composable
fun ClientApp(
    detectionViewModel: DetectionViewModel,
    dashboardViewModel: DashboardViewModel,
    baseUrl: String,
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.HISTORY) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = destination == AppDestination.HISTORY,
                    onClick = { destination = AppDestination.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("履歴") },
                )
                NavigationBarItem(
                    selected = destination == AppDestination.DASHBOARD,
                    onClick = { destination = AppDestination.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") },
                )
            }
        },
    ) { innerPadding ->
        when (destination) {
            AppDestination.HISTORY -> DetectionListScreen(
                viewModel = detectionViewModel,
                baseUrl = baseUrl,
                modifier = Modifier.padding(innerPadding),
            )
            AppDestination.DASHBOARD -> DashboardScreen(
                viewModel = dashboardViewModel,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
