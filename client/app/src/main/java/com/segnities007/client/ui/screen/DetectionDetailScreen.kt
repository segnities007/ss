package com.segnities007.client.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.segnities007.client.model.BoundingBox
import com.segnities007.client.model.Detection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                title = { Text("検知詳細") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "検知履歴へ戻る",
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
            DetectionImage(
                imageUrl = resolveImageUrl(detection.imageUrl, baseUrl),
                contentDescription = "${labelForType(detection.type)}の検知画像",
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = labelForType(detection.type),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = descriptionForType(detection.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                DetailCard(
                    title = "判定情報",
                    rows = listOf(
                        "検知日時" to formatTimestamp(detection.detectedAt),
                        "信頼度" to detection.confidence
                            ?.let(::formatConfidence)
                            .orEmpty()
                            .ifEmpty { "記録なし" },
                        "Tracking ID" to trackingId(detection),
                        "継続時間" to detection.metadata?.durationSeconds
                            ?.let { "$it 秒" }
                            .orEmpty()
                            .ifEmpty { "記録なし" },
                    ),
                )

                DetailCard(
                    title = "検出位置",
                    rows = buildList {
                        add("検出枠" to formatBoundingBox(detection.metadata?.boundingBox))
                        detection.metadata?.location?.let {
                            add("基準位置" to formatBoundingBox(it))
                        }
                    },
                )

                DetailCard(
                    title = "記録情報",
                    rows = buildList {
                        add("記録ID" to detection.id.toString())
                        detection.createdAt?.let {
                            add("保存日時" to formatTimestamp(it))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DetectionImage(
    imageUrl: String,
    contentDescription: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
        )
    }
}

@Composable
private fun DetailCard(
    title: String,
    rows: List<Pair<String, String>>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                DetailRow(label = label, value = value)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(2f),
        )
    }
}

private fun resolveImageUrl(imageUrl: String, baseUrl: String): String {
    return if (imageUrl.startsWith("http")) {
        imageUrl
    } else {
        baseUrl.removeSuffix("/") + imageUrl
    }
}

private fun labelForType(type: String): String {
    return when (type) {
        "person" -> "不審者"
        "suspicious_vehicle" -> "不審車両"
        else -> type
    }
}

private fun descriptionForType(type: String): String {
    return when (type) {
        "person" -> "人物が設定時間以上継続して検知されました。"
        "suspicious_vehicle" -> "車両が同じ位置に設定時間以上留まっていると判定されました。"
        else -> "監視システムによって検知された記録です。"
    }
}

private fun trackingId(detection: Detection): String {
    val trackId = detection.metadata?.trackId ?: return "記録なし"
    val prefix = if (detection.type == "person") "P" else "V"
    return "$prefix-$trackId"
}

private fun formatConfidence(confidence: Double): String {
    return String.format(Locale.JAPAN, "%.1f%%", confidence * 100)
}

private fun formatBoundingBox(boundingBox: BoundingBox?): String {
    if (boundingBox == null) return "記録なし"
    return "x=${boundingBox.x}, y=${boundingBox.y}, " +
        "${boundingBox.width} × ${boundingBox.height} px"
}

private fun formatTimestamp(value: String): String {
    return runCatching {
        DATE_TIME_FORMATTER.format(Instant.parse(value).atZone(ZoneId.systemDefault()))
    }.getOrDefault(value)
}

private val DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
