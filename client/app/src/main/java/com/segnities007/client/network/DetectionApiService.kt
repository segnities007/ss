package com.segnities007.client.network

import com.segnities007.client.model.Detection
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DetectionApiService {
    @GET("api/detections")
    suspend fun getDetections(
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 50,
    ): Response<List<Detection>>
}
