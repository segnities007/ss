package com.segnities007.service

import com.segnities007.model.Detection
import com.segnities007.model.DetectionMetadata
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

class DetectionService(val database: R2dbcDatabase) {

    object Detections : UIntIdTable() {
        val type = varchar("type", length = 32)
        val detectedAt = varchar("detected_at", length = 64)
        val confidence = double("confidence").nullable()
        val imagePath = varchar("image_path", length = 512)
        val metadata = text("metadata").nullable()
        val createdAt = varchar("created_at", length = 64)
    }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun createSchema() {
        suspendTransaction(database) {
            SchemaUtils.create(Detections)
        }
    }

    suspend fun create(
        type: String,
        detectedAt: String,
        confidence: Double?,
        imagePath: String,
        metadata: DetectionMetadata?,
    ): Detection {
        val metadataJson = metadata?.let { json.encodeToString(it) }
        val createdAt = java.time.Instant.now().toString()
        return suspendTransaction(database) {
            val newRecord = Detections.insert {
                it[Detections.type] = type
                it[Detections.detectedAt] = detectedAt
                it[Detections.confidence] = confidence
                it[Detections.imagePath] = imagePath
                it[Detections.metadata] = metadataJson
                it[Detections.createdAt] = createdAt
            }
            val id = newRecord[Detections.id].value
            Detection(
                id = id,
                type = newRecord[Detections.type],
                detectedAt = newRecord[Detections.detectedAt],
                confidence = newRecord[Detections.confidence],
                imageUrl = "/api/detections/$id/image",
                metadata = metadataJson?.let { json.decodeFromString(it) },
                createdAt = newRecord[Detections.createdAt],
            )
        }
    }

    suspend fun list(type: String? = null, limit: Int = 50): List<Detection> {
        return suspendTransaction(database) {
            Detections.selectAll()
                .let { query ->
                    if (type != null) {
                        query.where { Detections.type eq type }
                    } else {
                        query
                    }
                }
                .orderBy(Detections.detectedAt, SortOrder.DESC)
                .limit(limit)
                .map { rowToDetection(it) }
                .toList()
        }
    }

    suspend fun getImagePath(id: UInt): String? {
        return suspendTransaction(database) {
            Detections.selectAll()
                .where { Detections.id eq id }
                .map { it[Detections.imagePath] }
                .singleOrNull()
        }
    }

    private fun rowToDetection(row: ResultRow): Detection {
        val id = row[Detections.id].value
        val metadataJson = row[Detections.metadata]
        return Detection(
            id = id,
            type = row[Detections.type],
            detectedAt = row[Detections.detectedAt],
            confidence = row[Detections.confidence],
            imageUrl = "/api/detections/$id/image",
            metadata = metadataJson?.let { json.decodeFromString(it) },
            createdAt = row[Detections.createdAt],
        )
    }
}
