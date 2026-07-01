package com.segnities007.client.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.client.R
import com.segnities007.client.model.IoTDeviceSettings

@Composable
fun DeviceSettingsCard(
    settings: IoTDeviceSettings,
    isUpdating: Boolean,
    onBuzzerChanged: (Boolean) -> Unit,
    onCameraChanged: (Boolean) -> Unit,
    onPirSensorChanged: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.device_settings),
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
                title = stringResource(R.string.buzzer),
                checked = settings.buzzerEnabled,
                enabled = !isUpdating,
                onCheckedChange = onBuzzerChanged,
            )
            DeviceSettingRow(
                title = stringResource(R.string.usb_camera),
                checked = settings.cameraEnabled,
                enabled = !isUpdating,
                onCheckedChange = onCameraChanged,
            )
            DeviceSettingRow(
                title = stringResource(R.string.pir_sensor),
                checked = settings.pirSensorEnabled,
                enabled = !isUpdating,
                onCheckedChange = onPirSensorChanged,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceSettingsCardPreview() {
    MaterialTheme {
        DeviceSettingsCard(
            settings = IoTDeviceSettings(
                buzzerEnabled = true,
                cameraEnabled = false,
                pirSensorEnabled = true,
            ),
            isUpdating = false,
            onBuzzerChanged = {},
            onCameraChanged = {},
            onPirSensorChanged = {},
        )
    }
}
