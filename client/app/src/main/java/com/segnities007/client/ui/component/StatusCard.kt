package com.segnities007.client.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.client.R
import com.segnities007.client.model.IoTControlStatus
import com.segnities007.client.model.IoTDeviceSettings
import com.segnities007.client.ui.util.formatLastSeen

@Composable
fun StatusCard(status: IoTControlStatus) {
    val containerColor = if (status.deviceOnline) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (status.deviceOnline) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.raspberry_pi),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            StatusRow(
                label = stringResource(R.string.connection),
                value = if (status.deviceOnline) {
                    stringResource(R.string.online)
                } else {
                    stringResource(R.string.offline)
                },
            )
            StatusRow(
                label = stringResource(R.string.last_communication),
                value = formatLastSeen(status.lastSeenAt),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusCardOnlinePreview() {
    MaterialTheme {
        StatusCard(
            status = IoTControlStatus(
                deviceOnline = true,
                monitoringEnabled = true,
                monitoringActive = true,
                settings = IoTDeviceSettings(),
                lastSeenAt = "2026-07-01T12:00:00Z",
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusCardOfflinePreview() {
    MaterialTheme {
        StatusCard(
            status = IoTControlStatus(
                deviceOnline = false,
                monitoringEnabled = true,
                monitoringActive = false,
                settings = IoTDeviceSettings(),
                lastSeenAt = null,
            ),
        )
    }
}
