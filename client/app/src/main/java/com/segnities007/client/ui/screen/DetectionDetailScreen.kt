package com.segnities007.client.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.client.R
import com.segnities007.client.model.BoundingBox
import com.segnities007.client.model.Detection
import com.segnities007.client.model.DetectionMetadata
import com.segnities007.client.ui.component.DetailCard
import com.segnities007.client.ui.component.DetectionImageCard
import com.segnities007.client.ui.util.formatBoundingBox
import com.segnities007.client.ui.util.formatConfidence
import com.segnities007.client.ui.util.formatTimestamp
import com.segnities007.client.ui.util.labelForType
import com.segnities007.client.ui.util.resolveImageUrl
import com.segnities007.client.ui.util.trackingId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionDetailScreen(
    detection: Detection,
    baseUrl: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detection_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_history),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetectionImageCard(
                imageUrl = resolveImageUrl(detection.imageUrl, baseUrl),
                contentDescription = "${labelForType(detection.type)}${stringResource(R.string.detection_image)}",
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = labelForType(detection.type),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                val noRecord = stringResource(R.string.no_record)
                val secondsUnit = stringResource(R.string.seconds_unit)

                DetailCard(
                    title = stringResource(R.string.judgment_info),
                    rows = listOf(
                        stringResource(R.string.detection_datetime) to formatTimestamp(detection.detectedAt),
                        stringResource(R.string.confidence) to detection.confidence
                            ?.let { formatConfidence(it) }
                            .orEmpty()
                            .ifEmpty { noRecord },
                        stringResource(R.string.tracking_id) to trackingId(detection, noRecord),
                        stringResource(R.string.duration) to detection.metadata?.durationSeconds
                            ?.let { "$it $secondsUnit" }
                            .orEmpty()
                            .ifEmpty { noRecord },
                    ),
                )

                DetailCard(
                    title = stringResource(R.string.detection_location),
                    rows = buildList {
                        add(
                            stringResource(R.string.detection_box) to
                                    formatBoundingBox(detection.metadata?.boundingBox, noRecord),
                        )
                        detection.metadata?.location?.let {
                            add(
                                stringResource(R.string.reference_position) to
                                        formatBoundingBox(it, noRecord),
                            )
                        }
                    },
                )

                DetailCard(
                    title = stringResource(R.string.record_info),
                    rows = buildList {
                        add(stringResource(R.string.record_id) to detection.id.toString())
                        detection.createdAt?.let {
                            add(stringResource(R.string.saved_datetime) to formatTimestamp(it))
                        }
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetectionDetailScreenPreview() {
    MaterialTheme {
        DetectionDetailScreen(
            detection = Detection(
                id = 1u,
                type = "person",
                detectedAt = "2026-07-01T12:00:00Z",
                confidence = 0.875,
                imageUrl = "",
                metadata = DetectionMetadata(
                    durationSeconds = 60,
                    trackId = 7,
                    boundingBox = BoundingBox(x = 10, y = 20, width = 100, height = 200),
                    location = BoundingBox(x = 0, y = 0, width = 640, height = 480),
                ),
                createdAt = "2026-07-01T12:00:05Z",
            ),
            baseUrl = "http://localhost:8080",
            onBack = {},
        )
    }
}
