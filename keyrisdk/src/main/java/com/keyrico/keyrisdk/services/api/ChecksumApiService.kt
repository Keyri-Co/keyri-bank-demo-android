package com.keyrico.keyrisdk.services.api

import com.keyrico.keyrisdk.entity.checksums.request.ChecksumCheckRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface ChecksumApiService {
    @POST("test")
    suspend fun sendChecksums(
        @Header("x-api-key") apiKey: String,
        @Body request: ChecksumCheckRequest,
    ): Response<Unit>
}
