package com.segnities007

import com.segnities007.model.IoTControlStatus
import com.segnities007.routing.iotControlRouting
import com.segnities007.service.IoTControlService
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IoTControlRoutingTest {

    @Test
    fun `mobile control and IoT heartbeat share monitoring state`() = testApplication {
        application {
            configureSerialization()
            iotControlRouting(IoTControlService())
        }

        val stoppedResponse = client.put("/api/iot/control") {
            contentType(ContentType.Application.Json)
            setBody("""{"monitoringEnabled":false}""")
        }
        assertEquals(HttpStatusCode.OK, stoppedResponse.status)
        val stopped = Json.decodeFromString<IoTControlStatus>(stoppedResponse.bodyAsText())
        assertFalse(stopped.monitoringEnabled)
        assertFalse(stopped.deviceOnline)

        val settingsResponse = client.put("/api/iot/settings") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"buzzerEnabled":false,"cameraEnabled":true,"pirSensorEnabled":false}"""
            )
        }
        assertEquals(HttpStatusCode.OK, settingsResponse.status)
        val settingsStatus = Json.decodeFromString<IoTControlStatus>(
            settingsResponse.bodyAsText()
        )
        assertFalse(settingsStatus.settings.buzzerEnabled)
        assertTrue(settingsStatus.settings.cameraEnabled)
        assertFalse(settingsStatus.settings.pirSensorEnabled)

        val heartbeatResponse = client.post("/api/iot/heartbeat") {
            contentType(ContentType.Application.Json)
            setBody("""{"monitoringActive":false}""")
        }
        val heartbeat = Json.decodeFromString<IoTControlStatus>(heartbeatResponse.bodyAsText())
        assertFalse(heartbeat.monitoringEnabled)
        assertTrue(heartbeat.deviceOnline)
        assertFalse(heartbeat.monitoringActive)

        val currentResponse = client.get("/api/iot/control")
        assertEquals(HttpStatusCode.OK, currentResponse.status)
        val current = Json.decodeFromString<IoTControlStatus>(currentResponse.bodyAsText())
        assertFalse(current.monitoringEnabled)
        assertTrue(current.deviceOnline)
        assertFalse(current.settings.buzzerEnabled)
        assertTrue(current.settings.cameraEnabled)
        assertFalse(current.settings.pirSensorEnabled)
    }
}
