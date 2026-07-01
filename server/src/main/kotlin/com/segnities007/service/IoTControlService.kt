package com.segnities007.service

import com.segnities007.model.IoTControlStatus
import com.segnities007.model.IoTDeviceSettings
import java.time.Duration
import java.time.Instant

class IoTControlService(
    private val clock: () -> Instant = Instant::now,
    private val onlineTimeout: Duration = Duration.ofSeconds(15),
) {
    private val lock = Any()
    private var monitoringEnabled = true
    private var monitoringActive = false
    private var lastSeenAt: Instant? = null
    private var settings = IoTDeviceSettings()

    fun getStatus(): IoTControlStatus = synchronized(lock) {
        statusAt(clock())
    }

    fun setMonitoringEnabled(enabled: Boolean): IoTControlStatus = synchronized(lock) {
        monitoringEnabled = enabled
        statusAt(clock())
    }

    fun recordHeartbeat(active: Boolean): IoTControlStatus = synchronized(lock) {
        val now = clock()
        monitoringActive = active
        lastSeenAt = now
        statusAt(now)
    }

    fun updateSettings(newSettings: IoTDeviceSettings): IoTControlStatus = synchronized(lock) {
        settings = newSettings
        statusAt(clock())
    }

    private fun statusAt(now: Instant): IoTControlStatus {
        val lastSeen = lastSeenAt
        val online = lastSeen != null && Duration.between(lastSeen, now) <= onlineTimeout
        return IoTControlStatus(
            monitoringEnabled = monitoringEnabled,
            monitoringActive = online && monitoringActive,
            deviceOnline = online,
            lastSeenAt = lastSeen?.toString(),
            settings = settings,
        )
    }
}
