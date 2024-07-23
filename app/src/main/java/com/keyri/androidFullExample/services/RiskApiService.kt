package com.keyri.androidFullExample.services

import com.keyri.androidFullExample.services.entities.requests.DecryptRiskRequest
import com.keyri.androidFullExample.services.entities.responses.DecryptRiskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RiskApiService {
    @POST("api/decrypt-risk")
    suspend fun decryptRisk(
        @Body request: DecryptRiskRequest,
    ): Response<DecryptRiskResponse>
}
