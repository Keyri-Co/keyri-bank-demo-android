package com.keyri.androidFullExample.services

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TestApiService {
    @POST("api/decrypt-risk")
    suspend fun test(
        @Body request: TestRequest,
    ): Response<Unit>
}

data class TestRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("fcmToken")
    val fcmToken: String,
)
