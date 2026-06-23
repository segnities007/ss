package com.segnities007

import com.segnities007.service.DetectionService
import io.ktor.server.application.*
import org.koin.ktor.ext.inject

suspend fun Application.configureExposed() {
    val detectionService: DetectionService by inject()
    detectionService.createSchema()
}
