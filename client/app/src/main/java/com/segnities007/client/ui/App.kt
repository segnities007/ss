package com.segnities007.client.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.segnities007.client.model.Detection
import com.segnities007.client.ui.component.AppNavigationBar
import com.segnities007.client.ui.screen.DetectionDetailScreen
import com.segnities007.client.ui.screen.DetectionListScreen
import com.segnities007.client.ui.screen.SettingsScreen
import com.segnities007.client.ui.viewmodel.DetectionViewModel
import com.segnities007.client.ui.viewmodel.SettingsViewModel

private enum class AppDestination {
    HISTORY,
    SETTINGS,
}

@Composable
fun ClientApp(
    detectionViewModel: DetectionViewModel,
    settingsViewModel: SettingsViewModel,
    baseUrl: String,
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.HISTORY) }
    var selectedDetection by remember { mutableStateOf<Detection?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (selectedDetection == null) {
                AppNavigationBar(
                    historySelected = destination == AppDestination.HISTORY,
                    settingsSelected = destination == AppDestination.SETTINGS,
                    onHistoryClick = { destination = AppDestination.HISTORY },
                    onSettingsClick = { destination = AppDestination.SETTINGS },
                )
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
        val detection = selectedDetection

        if (detection != null) {
            DetectionDetailScreen(
                detection = detection,
                baseUrl = baseUrl,
                onBack = { selectedDetection = null },
                modifier = contentModifier,
            )
        } else {
            when (destination) {
                AppDestination.HISTORY -> DetectionListScreen(
                    viewModel = detectionViewModel,
                    baseUrl = baseUrl,
                    onDetectionClick = { selectedDetection = it },
                    modifier = contentModifier,
                )
                AppDestination.SETTINGS -> SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = contentModifier,
                )
            }
        }
    }
}
