package com.segnities007.model

import kotlinx.serialization.Serializable

@Serializable
data class IoTControlStatus(
    val monitoringEnabled: Boolean,
    val monitoringActive: Boolean,
    val deviceOnline: Boolean,
    val lastSeenAt: String? = null,
    val settings: IoTDeviceSettings,
)

@Serializable
data class IoTDeviceSettings(
    val buzzerEnabled: Boolean = true,
    val cameraEnabled: Boolean = true,
    val pirSensorEnabled: Boolean = true,
)

@Serializable
data class UpdateIoTControlRequest(
    val monitoringEnabled: Boolean,
)

@Serializable
data class IoTHeartbeatRequest(
    val monitoringActive: Boolean,
)
