package com.segnities007.service

import com.segnities007.model.Detection
import com.segnities007.model.DetectionMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DetectionService(val database: Database) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun createSchema() {
        withContext(Dispatchers.IO) {
            transaction(database) {
                SchemaUtils.create(DetectionsTable)
            }
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
        return withContext(Dispatchers.IO) {
            transaction(database) {
                val newRecord = DetectionsTable.insert {
                    it[DetectionsTable.type] = type
                    it[DetectionsTable.detectedAt] = detectedAt
                    it[DetectionsTable.confidence] = confidence
                    it[DetectionsTable.imagePath] = imagePath
                    it[DetectionsTable.metadata] = metadataJson
                    it[DetectionsTable.createdAt] = createdAt
                }
                val id = newRecord[DetectionsTable.id].value
                Detection(
                    id = id,
                    type = newRecord[DetectionsTable.type],
                    detectedAt = newRecord[DetectionsTable.detectedAt],
                    confidence = newRecord[DetectionsTable.confidence],
                    imageUrl = "/api/detections/$id/image",
                    metadata = metadataJson?.let { json.decodeFromString(it) },
                    createdAt = newRecord[DetectionsTable.createdAt],
                )
            }
        }
    }

    suspend fun list(type: String? = null, limit: Int = 50): List<Detection> {
        return withContext(Dispatchers.IO) {
            transaction(database) {
                DetectionsTable.selectAll()
                    .let { query ->
                        if (type != null) {
                            query.where { DetectionsTable.type eq type }
                        } else {
                            query
                        }
                    }
                    .orderBy(DetectionsTable.detectedAt, SortOrder.DESC)
                    .limit(limit)
                    .map { rowToDetection(it) }
            }
        }
    }

    suspend fun getImagePath(id: UInt): String? {
        return withContext(Dispatchers.IO) {
            transaction(database) {
                DetectionsTable.selectAll()
                    .where { DetectionsTable.id eq id }
                    .map { it[DetectionsTable.imagePath] }
                    .singleOrNull()
            }
        }
    }

    private fun rowToDetection(row: ResultRow): Detection {
        val id = row[DetectionsTable.id].value
        val metadataJson = row[DetectionsTable.metadata]
        return Detection(
            id = id,
            type = row[DetectionsTable.type],
            detectedAt = row[DetectionsTable.detectedAt],
            confidence = row[DetectionsTable.confidence],
            imageUrl = "/api/detections/$id/image",
            metadata = metadataJson?.let { json.decodeFromString(it) },
            createdAt = row[DetectionsTable.createdAt],
        )
    }
}
