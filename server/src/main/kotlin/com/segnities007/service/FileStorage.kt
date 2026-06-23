package com.segnities007.service

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.util.UUID

class FileStorage(private val baseDir: String = "uploads") {

    init {
        Files.createDirectories(Paths.get(baseDir))
    }

    fun save(bytes: ByteArray, extension: String): String {
        val sanitizedExtension = extension.lowercase().removePrefix(".")
        val fileName = "${UUID.randomUUID()}_${Instant.now().toEpochMilli()}.$sanitizedExtension"
        val path = Paths.get(baseDir, fileName)
        Files.write(path, bytes)
        return path.toString()
    }

    fun read(path: String): ByteArray? {
        val file = File(path)
        return if (file.exists() && file.isFile) file.readBytes() else null
    }

    fun contentType(path: String): String {
        return when (path.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
