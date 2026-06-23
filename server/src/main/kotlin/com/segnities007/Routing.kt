package com.segnities007

import com.segnities007.routing.detectionRouting
import com.segnities007.service.DetectionService
import com.segnities007.service.FileStorage
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val detectionService: DetectionService by inject()
    val fileStorage: FileStorage by inject()

    routing {
        get("/") {
            call.respondText("Suspicious Person/Vehicle Detection Server")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }

    detectionRouting(detectionService, fileStorage)
}
