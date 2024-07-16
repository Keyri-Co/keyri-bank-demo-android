package com.keyri.androidFullExample.repositories

import com.keyri.androidFullExample.services.ApiService
import com.keyri.androidFullExample.services.entities.requests.CryptoLoginRequest
import com.keyri.androidFullExample.services.entities.requests.CryptoRegisterRequest
import com.keyri.androidFullExample.services.entities.requests.EmailLoginRequest
import com.keyri.androidFullExample.services.entities.requests.ReverseSmsLoginRequest
import com.keyri.androidFullExample.services.entities.responses.KeyriResponse
import com.keyri.androidFullExample.services.makeApiCall

class KeyriDemoRepository(private val apiService: ApiService) {

    suspend fun cryptoRegister(email: String, associationKey: String): KeyriResponse {
        return makeApiCall {
            apiService.cryptoRegister(
                CryptoRegisterRequest(
                    email,
                    associationKey
                )
            )
        }.getOrThrow()
    }

    suspend fun cryptoLogin(email: String, data: String, signatureB64: String): KeyriResponse {
        return makeApiCall {
            apiService.cryptoLogin(
                CryptoLoginRequest(
                    email,
                    data,
                    signatureB64
                )
            )
        }.getOrThrow()
    }

    suspend fun emailLogin(email: String): KeyriResponse {
        return makeApiCall { apiService.emailLogin(EmailLoginRequest(email)) }.getOrThrow()
    }

    suspend fun smsLogin(number: String): KeyriResponse {
        return makeApiCall { apiService.smsLogin(ReverseSmsLoginRequest(number)) }.getOrThrow()
    }
}
