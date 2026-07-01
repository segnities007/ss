package com.segnities007.client.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.client.model.IoTControlStatus
import com.segnities007.client.model.IoTDeviceSettings
import com.segnities007.client.ui.component.DeviceSettingRow
import com.segnities007.client.ui.component.StatusCard
import com.segnities007.client.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
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
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新")
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

            status != null -> DashboardContent(
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
                    Text("IoT状態を取得できません")
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
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

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "監視制御",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (status.monitoringEnabled) "監視中" else "停止中",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (
                        status.deviceOnline &&
                        status.monitoringEnabled != status.monitoringActive
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Raspberry Piへの反映を待っています",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                Switch(
                    checked = status.monitoringEnabled,
                    onCheckedChange = onMonitoringChanged,
                    enabled = !isUpdating,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "デバイス設定",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 12.dp,
                        bottom = 4.dp,
                    ),
                )
                DeviceSettingRow(
                    title = "ブザー",
                    checked = status.settings.buzzerEnabled,
                    enabled = !isUpdating,
                    onCheckedChange = onBuzzerChanged,
                )
                DeviceSettingRow(
                    title = "USB camera",
                    checked = status.settings.cameraEnabled,
                    enabled = !isUpdating,
                    onCheckedChange = onCameraChanged,
                )
                DeviceSettingRow(
                    title = "PIR motion sensor",
                    checked = status.settings.pirSensorEnabled,
                    enabled = !isUpdating,
                    onCheckedChange = onPirSensorChanged,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    MaterialTheme {
        DashboardContent(
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
