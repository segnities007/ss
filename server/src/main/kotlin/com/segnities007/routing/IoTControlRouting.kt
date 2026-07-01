package com.segnities007.routing

import com.segnities007.model.IoTHeartbeatRequest
import com.segnities007.model.IoTDeviceSettings
import com.segnities007.model.UpdateIoTControlRequest
import com.segnities007.service.IoTControlService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.iotControlRouting(iotControlService: IoTControlService) {
    routing {
        route("/api/iot") {
            get("/control") {
                call.respond(HttpStatusCode.OK, iotControlService.getStatus())
            }

            put("/control") {
                val request = call.receive<UpdateIoTControlRequest>()
                call.respond(
                    HttpStatusCode.OK,
                    iotControlService.setMonitoringEnabled(request.monitoringEnabled),
                )
            }

            put("/settings") {
                val settings = call.receive<IoTDeviceSettings>()
                call.respond(
                    HttpStatusCode.OK,
                    iotControlService.updateSettings(settings),
                )
            }

            post("/heartbeat") {
                val request = call.receive<IoTHeartbeatRequest>()
                call.respond(
                    HttpStatusCode.OK,
                    iotControlService.recordHeartbeat(request.monitoringActive),
                )
            }
        }
    }
}
