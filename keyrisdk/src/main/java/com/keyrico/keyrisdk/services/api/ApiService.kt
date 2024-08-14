package com.keyrico.keyrisdk.services.api

import com.keyrico.keyrisdk.entity.session.InternalSession
import com.keyrico.keyrisdk.entity.session.SessionConfirmationResponse
import com.keyrico.keyrisdk.services.api.data.SessionConfirmationRequest
import com.keyrico.keyrisdk.utils.API_VERSION
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ApiService {
    @Headers("x-mobile-os: android")
    @GET("api/$API_VERSION/session/{sessionId}")
    suspend fun getSession(
        @Header("x-mobile-id") mobileId: String,
        @Header("x-mobile-vendorId") vendorId: String?,
        @Path("sessionId") sessionId: String,
        @Query("appKey") appKey: String,
    ): Response<InternalSession>

    @POST("api/$API_VERSION/session/{sessionId}")
    suspend fun approveSession(
        @Path("sessionId") sessionId: String,
        @Body request: SessionConfirmationRequest,
    ): Response<SessionConfirmationResponse>
}
