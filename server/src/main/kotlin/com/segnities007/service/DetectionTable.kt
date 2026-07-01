package com.segnities007.service

import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable

object DetectionsTable : UIntIdTable() {
    val type = varchar("type", length = 32)
    val detectedAt = varchar("detected_at", length = 64)
    val confidence = double("confidence").nullable()
    val imagePath = varchar("image_path", length = 512)
    val metadata = text("metadata").nullable()
    val createdAt = varchar("created_at", length = 64)
}
