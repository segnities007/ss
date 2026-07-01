package com.segnities007.client.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.client.R
import com.segnities007.client.model.IoTControlStatus
import com.segnities007.client.model.IoTDeviceSettings

@Composable
fun MonitoringControlCard(
    status: IoTControlStatus,
    isUpdating: Boolean,
    onMonitoringChanged: (Boolean) -> Unit,
) {
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
                    text = stringResource(R.string.monitoring_control),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (status.monitoringEnabled) {
                        stringResource(R.string.monitoring_active)
                    } else {
                        stringResource(R.string.monitoring_inactive)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (status.deviceOnline && status.monitoringEnabled != status.monitoringActive) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.waiting_for_pi_reflection),
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
}

@Preview(showBackground = true)
@Composable
private fun MonitoringControlCardPreview() {
    MaterialTheme {
        MonitoringControlCard(
            status = IoTControlStatus(
                deviceOnline = true,
                monitoringEnabled = true,
                monitoringActive = false,
                settings = IoTDeviceSettings(),
            ),
            isUpdating = false,
            onMonitoringChanged = {},
        )
    }
}
