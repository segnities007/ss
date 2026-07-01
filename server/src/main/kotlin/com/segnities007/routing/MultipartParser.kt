package com.segnities007.routing

import com.segnities007.model.DetectionMetadata
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import kotlinx.serialization.json.Json

data class DetectionMultipartData(
    val type: String?,
    val detectedAt: String?,
    val confidence: Double?,
    val metadata: DetectionMetadata?,
    val imageBytes: ByteArray?,
    val imageExtension: String,
)

suspend fun ApplicationCall.parseDetectionMultipart(): DetectionMultipartData {
    val json = Json { ignoreUnknownKeys = true }
    val multipart = receiveMultipart()

    var type: String? = null
    var detectedAt: String? = null
    var confidence: Double? = null
    var metadata: DetectionMetadata? = null
    var imageBytes: ByteArray? = null
    var imageExtension: String = "jpg"

    multipart.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                when (part.name) {
                    "type" -> type = part.value
                    "detectedAt" -> detectedAt = part.value
                    "confidence" -> confidence = part.value.toDoubleOrNull()
                    "metadata" -> metadata = json.decodeFromString<DetectionMetadata>(part.value)
                }
            }
            is PartData.FileItem -> {
                if (part.name == "image") {
                    imageBytes = part.streamProvider().readBytes()
                    imageExtension = part.originalFileName
                        ?.substringAfterLast('.', "jpg")
                        ?.lowercase()
                        ?: "jpg"
                }
            }
            else -> {}
        }
        part.dispose()
    }

    return DetectionMultipartData(
        type = type,
        detectedAt = detectedAt,
        confidence = confidence,
        metadata = metadata,
        imageBytes = imageBytes,
        imageExtension = imageExtension,
    )
}
