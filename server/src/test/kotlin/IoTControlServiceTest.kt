package com.segnities007

import com.segnities007.service.IoTControlService
import com.segnities007.model.IoTDeviceSettings
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IoTControlServiceTest {

    @Test
    fun `control can stop and restart monitoring`() {
        val service = IoTControlService()

        assertTrue(service.getStatus().monitoringEnabled)
        assertFalse(service.setMonitoringEnabled(false).monitoringEnabled)
        assertTrue(service.setMonitoringEnabled(true).monitoringEnabled)
    }

    @Test
    fun `heartbeat reports device online until timeout`() {
        var now = Instant.parse("2026-07-01T00:00:00Z")
        val service = IoTControlService(
            clock = { now },
            onlineTimeout = Duration.ofSeconds(15),
        )

        val heartbeat = service.recordHeartbeat(active = true)
        assertTrue(heartbeat.deviceOnline)
        assertTrue(heartbeat.monitoringActive)

        now = now.plusSeconds(16)
        val timedOut = service.getStatus()
        assertFalse(timedOut.deviceOnline)
        assertFalse(timedOut.monitoringActive)
    }

    @Test
    fun `device settings can be changed independently`() {
        val service = IoTControlService()

        val status = service.updateSettings(
            IoTDeviceSettings(
                buzzerEnabled = false,
                cameraEnabled = true,
                pirSensorEnabled = false,
            )
        )

        assertFalse(status.settings.buzzerEnabled)
        assertTrue(status.settings.cameraEnabled)
        assertFalse(status.settings.pirSensorEnabled)
    }
}
