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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.segnities007.client.model.IoTControlStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

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
                text = "Raspberry Pi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            StatusRow("接続", if (status.deviceOnline) "オンライン" else "オフライン")
            StatusRow(
                "監視状態",
                when {
                    !status.deviceOnline -> "確認できません"
                    status.monitoringActive -> "監視中"
                    else -> "停止中"
                },
            )
            StatusRow("最終通信", formatLastSeen(status.lastSeenAt))
        }
    }
}

private fun formatLastSeen(value: String?): String {
    if (value == null) return "未接続"
    return runCatching {
        LAST_SEEN_FORMATTER
            .format(Instant.parse(value)
                .atZone(ZoneId.systemDefault()))
    }.getOrDefault(value)
}

private val LAST_SEEN_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
