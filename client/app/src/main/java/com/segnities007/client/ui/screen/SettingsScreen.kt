package com.segnities007.client.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.client.R
import com.segnities007.client.model.IoTControlStatus
import com.segnities007.client.model.IoTDeviceSettings
import com.segnities007.client.ui.component.DeviceSettingsCard
import com.segnities007.client.ui.component.MonitoringControlCard
import com.segnities007.client.ui.component.StatusCard
import com.segnities007.client.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isUpdating by viewModel.isUpdating.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            status == null && isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            status != null -> SettingsContent(
                status = checkNotNull(status),
                isUpdating = isUpdating,
                onMonitoringChanged = viewModel::setMonitoringEnabled,
                onBuzzerChanged = viewModel::setBuzzerEnabled,
                onCameraChanged = viewModel::setCameraEnabled,
                onPirSensorChanged = viewModel::setPirSensorEnabled,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.iot_status_unavailable))
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    status: IoTControlStatus,
    isUpdating: Boolean,
    onMonitoringChanged: (Boolean) -> Unit,
    onBuzzerChanged: (Boolean) -> Unit,
    onCameraChanged: (Boolean) -> Unit,
    onPirSensorChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusCard(status)
        MonitoringControlCard(
            status = status,
            isUpdating = isUpdating,
            onMonitoringChanged = onMonitoringChanged,
        )
        DeviceSettingsCard(
            settings = status.settings,
            isUpdating = isUpdating,
            onBuzzerChanged = onBuzzerChanged,
            onCameraChanged = onCameraChanged,
            onPirSensorChanged = onPirSensorChanged,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    MaterialTheme {
        SettingsContent(
            status = IoTControlStatus(
                deviceOnline = true,
                monitoringEnabled = true,
                monitoringActive = false,
                settings = IoTDeviceSettings(
                    buzzerEnabled = true,
                    cameraEnabled = true,
                    pirSensorEnabled = false,
                ),
            ),
            isUpdating = false,
            onMonitoringChanged = {},
            onBuzzerChanged = {},
            onCameraChanged = {},
            onPirSensorChanged = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
