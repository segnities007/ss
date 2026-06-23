package com.segnities007.routing

import com.segnities007.model.DetectionMetadata
import com.segnities007.service.DetectionService
import com.segnities007.service.FileStorage
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun Application.detectionRouting(
    detectionService: DetectionService,
    fileStorage: FileStorage,
) {
    val json = Json { ignoreUnknownKeys = true }

    routing {
        route("/api") {

            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }

            post("/detections") {
                val multipart = call.receiveMultipart()

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

                if (type == null || detectedAt == null || imageBytes == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing required fields: type, detectedAt, image")
                    return@post
                }

                val imagePath = fileStorage.save(imageBytes!!, imageExtension)
                val detection = detectionService.create(
                    type = type!!,
                    detectedAt = detectedAt!!,
                    confidence = confidence,
                    imagePath = imagePath,
                    metadata = metadata,
                )

                call.respond(HttpStatusCode.Created, detection)
            }

            get("/detections") {
                val type = call.request.queryParameters["type"]
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

                val detections = detectionService.list(type, limit)
                call.respond(HttpStatusCode.OK, detections)
            }

            get("/detections/{id}/image") {
                val id = call.parameters["id"]?.toUIntOrNull()
                    ?: throw IllegalArgumentException("Invalid ID")

                val imagePath = detectionService.getImagePath(id)
                if (imagePath == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val bytes = fileStorage.read(imagePath)
                if (bytes == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val contentType = ContentType.parse(fileStorage.contentType(imagePath))
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Inline.withParameter(
                        ContentDisposition.Parameters.FileName,
                        imagePath.substringAfterLast('/')
                    ).toString()
                )
                call.respondBytes(bytes, contentType)
            }
        }
    }
}
