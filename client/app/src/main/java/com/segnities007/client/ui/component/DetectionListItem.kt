package com.segnities007.client.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.segnities007.client.R
import com.segnities007.client.model.Detection
import com.segnities007.client.model.DetectionMetadata
import com.segnities007.client.ui.util.labelForType
import com.segnities007.client.ui.util.formatTimestamp
import com.segnities007.client.ui.util.resolveImageUrl
import com.segnities007.client.ui.util.trackingIdPrefix

@Composable
fun DetectionListItem(
    detection: Detection,
    baseUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageUrl = resolveImageUrl(detection.imageUrl, baseUrl)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.detection_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
            ) {
                Text(
                    text = labelForType(detection.type),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = formatTimestamp(detection.detectedAt),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                detection.metadata?.trackId?.let { trackId ->
                    Text(
                        text = "${stringResource(R.string.tracking_id)}: ${trackingIdPrefix(detection.type)}-$trackId",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.show_detail),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetectionListItemPreview() {
    MaterialTheme {
        DetectionListItem(
            detection = Detection(
                id = 1u,
                type = "person",
                detectedAt = "2026/07/01 12:00:00",
                confidence = 0.875,
                imageUrl = "",
                metadata = DetectionMetadata(trackId = 42),
            ),
            baseUrl = "http://localhost:8080",
            onClick = {},
        )
    }
}
