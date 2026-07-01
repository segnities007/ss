package com.segnities007.client.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.segnities007.client.model.Detection

@Composable
fun DetectionListItem(
    detection: Detection,
    baseUrl: String,
    modifier: Modifier = Modifier,
) {
    val imageUrl = if (detection.imageUrl.startsWith("http")) {
        detection.imageUrl
    } else {
        baseUrl.removeSuffix("/") + detection.imageUrl
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Detection image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = labelForType(detection.type),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = detection.detectedAt,
                    style = MaterialTheme.typography.bodySmall,
                )
                detection.confidence?.let {
                    Text(
                        text = "Confidence: ${(it * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                detection.metadata?.trackId?.let { trackId ->
                    val prefix = if (detection.type == "person") "P" else "V"
                    Text(
                        text = "Tracking ID: $prefix-$trackId",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun labelForType(type: String): String {
    return when (type) {
        "person" -> "不審者"
        "suspicious_vehicle" -> "不審車両"
        else -> type
    }
}
