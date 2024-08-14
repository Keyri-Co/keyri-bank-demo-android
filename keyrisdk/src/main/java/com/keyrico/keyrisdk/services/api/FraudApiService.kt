package com.keyrico.keyrisdk.services.api

import com.keyrico.keyrisdk.entity.fingerprint.request.FingerprintEventRequest
import com.keyrico.keyrisdk.entity.fingerprint.response.FingerprintEventResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

internal interface FraudApiService {
    @POST("v1/client")
    suspend fun sendFingerprintEvent(
        @Body request: FingerprintEventRequest,
    ): Response<FingerprintEventResponse>
}
