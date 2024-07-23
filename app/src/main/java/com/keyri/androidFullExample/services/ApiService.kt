package com.keyri.androidFullExample.services

import com.keyri.androidFullExample.services.entities.requests.CryptoLoginRequest
import com.keyri.androidFullExample.services.entities.requests.CryptoRegisterRequest
import com.keyri.androidFullExample.services.entities.requests.DecryptRiskRequest
import com.keyri.androidFullExample.services.entities.requests.EmailLoginRequest
import com.keyri.androidFullExample.services.entities.requests.ReverseSmsLoginRequest
import com.keyri.androidFullExample.services.entities.requests.UserInformationResponse
import com.keyri.androidFullExample.services.entities.requests.UserRegisterRequest
import com.keyri.androidFullExample.services.entities.responses.DecryptRiskResponse
import com.keyri.androidFullExample.services.entities.responses.KeyriResponse
import com.keyri.androidFullExample.services.entities.responses.SmsLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("crypto-register")
    suspend fun cryptoRegister(
        @Body request: CryptoRegisterRequest,
    ): Response<KeyriResponse>

    @POST("crypto-login")
    suspend fun cryptoLogin(
        @Body request: CryptoLoginRequest,
    ): Response<KeyriResponse>

    @POST("email-login")
    suspend fun emailLogin(
        @Body request: EmailLoginRequest,
    ): Response<KeyriResponse>

    @POST("sms-login")
    suspend fun smsLogin(
        @Body request: ReverseSmsLoginRequest,
    ): Response<SmsLoginResponse>

    @POST("user-register")
    suspend fun userRegister(
        @Body request: UserRegisterRequest,
    ): Response<SmsLoginResponse>

    @POST("get-user-information")
    suspend fun getUserInformation(
        @Body request: EmailLoginRequest,
    ): Response<UserInformationResponse>
}
