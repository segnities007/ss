package com.segnities007.model

import kotlinx.serialization.Serializable

@Serializable
data class Detection(
    val id: UInt? = null,
    val type: String,
    val detectedAt: String,
    val confidence: Double? = null,
    val imageUrl: String? = null,
    val metadata: DetectionMetadata? = null,
    val createdAt: String? = null,
)

@Serializable
data class DetectionMetadata(
    val boundingBox: BoundingBox? = null,
    val durationSeconds: Int? = null,
    val location: BoundingBox? = null,
    val trackId: Int? = null,
)

@Serializable
data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
