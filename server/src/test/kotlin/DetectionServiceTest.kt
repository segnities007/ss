package com.segnities007

import com.segnities007.model.BoundingBox
import com.segnities007.model.DetectionMetadata
import com.segnities007.service.DetectionService
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.Database
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DetectionServiceTest {

    @Test
    fun `sqlite stores and reads a detection`() = runBlocking {
        val databaseFile = Files.createTempFile("detections-test-", ".db").toFile()
        try {
            val database = Database.connect(
                url = "jdbc:sqlite:${databaseFile.absolutePath}",
                driver = "org.sqlite.JDBC",
            )
            val service = DetectionService(database)
            service.createSchema()

            val imagePath = "uploads/test.jpg"
            val created = service.create(
                type = "person",
                detectedAt = "2026-06-28T12:00:00Z",
                confidence = 0.9,
                imagePath = imagePath,
                metadata = DetectionMetadata(
                    boundingBox = BoundingBox(10, 20, 30, 40),
                    durationSeconds = 60,
                    trackId = 7,
                ),
            )

            val id = assertNotNull(created.id)
            val detections = service.list(limit = 10)

            assertEquals(1, detections.size)
            assertEquals(created, detections.single())
            assertEquals(imagePath, service.getImagePath(id))
        } finally {
            databaseFile.delete()
        }
    }
}
