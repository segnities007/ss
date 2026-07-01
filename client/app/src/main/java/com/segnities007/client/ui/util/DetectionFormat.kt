package com.segnities007.client.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.segnities007.client.R
import com.segnities007.client.model.BoundingBox
import com.segnities007.client.model.Detection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun labelForType(type: String): String {
    return when (type) {
        "person" -> stringResource(R.string.type_person)
        "suspicious_vehicle" -> stringResource(R.string.type_suspicious_vehicle)
        else -> type
    }
}

@Composable
fun descriptionForType(type: String): String {
    return when (type) {
        "person" -> stringResource(R.string.description_person)
        "suspicious_vehicle" -> stringResource(R.string.description_vehicle)
        else -> stringResource(R.string.description_default)
    }
}

@Composable
fun formatConfidence(confidence: Double): String {
    return String.format(
        Locale.JAPAN,
        stringResource(R.string.confidence_format),
        confidence * 100,
    )
}

fun trackingIdPrefix(type: String): String {
    return if (type == "person") "P" else "V"
}

fun trackingId(detection: Detection, noRecordString: String): String {
    val trackId = detection.metadata?.trackId ?: return noRecordString
    val prefix = trackingIdPrefix(detection.type)
    return "$prefix-$trackId"
}

fun formatBoundingBox(boundingBox: BoundingBox?, noRecordString: String): String {
    if (boundingBox == null) return noRecordString
    return "x=${boundingBox.x}, y=${boundingBox.y}, " +
        "${boundingBox.width} × ${boundingBox.height} px"
}

fun formatTimestamp(value: String): String {
    return runCatching {
        DATE_TIME_FORMATTER.format(Instant.parse(value).atZone(ZoneId.systemDefault()))
    }.getOrDefault(value)
}

fun resolveImageUrl(imageUrl: String, baseUrl: String): String {
    return if (imageUrl.startsWith("http")) {
        imageUrl
    } else {
        baseUrl.removeSuffix("/") + imageUrl
    }
}

@Composable
fun formatLastSeen(value: String?): String {
    if (value == null) return stringResource(R.string.not_connected)
    return runCatching {
        LAST_SEEN_FORMATTER
            .format(Instant.parse(value).atZone(ZoneId.systemDefault()))
    }.getOrDefault(value)
}

private val DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

private val LAST_SEEN_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
