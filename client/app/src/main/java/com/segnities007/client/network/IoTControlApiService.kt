package com.segnities007.client.network

import com.segnities007.client.model.IoTControlStatus
import com.segnities007.client.model.IoTDeviceSettings
import com.segnities007.client.model.UpdateIoTControlRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface IoTControlApiService {
    @GET("api/iot/control")
    suspend fun getIoTControlStatus(): Response<IoTControlStatus>

    @PUT("api/iot/control")
    suspend fun updateIoTControl(
        @Body request: UpdateIoTControlRequest,
    ): Response<IoTControlStatus>

    @PUT("api/iot/settings")
    suspend fun updateIoTDeviceSettings(
        @Body settings: IoTDeviceSettings,
    ): Response<IoTControlStatus>
}
