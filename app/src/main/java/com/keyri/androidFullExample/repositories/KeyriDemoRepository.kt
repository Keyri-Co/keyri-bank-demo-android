package com.keyri.androidFullExample.repositories

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.keyri.androidFullExample.services.ApiService
import com.keyri.androidFullExample.services.RiskApiService
import com.keyri.androidFullExample.services.entities.requests.CryptoLoginRequest
import com.keyri.androidFullExample.services.entities.requests.CryptoRegisterRequest
import com.keyri.androidFullExample.services.entities.requests.DecryptRiskRequest
import com.keyri.androidFullExample.services.entities.requests.EmailLoginRequest
import com.keyri.androidFullExample.services.entities.requests.ReverseSmsLoginRequest
import com.keyri.androidFullExample.services.entities.requests.UserRegisterRequest
import com.keyri.androidFullExample.services.entities.responses.DecryptRiskResponse
import com.keyri.androidFullExample.services.entities.responses.KeyriResponse
import com.keyri.androidFullExample.services.entities.responses.SmsLoginResponse
import com.keyri.androidFullExample.services.makeApiCall
import com.keyri.androidFullExample.utils.PHONE_PREFIX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response

class KeyriDemoRepository(
    private val apiService: ApiService,
    private val riskApiService: RiskApiService,
) {
    suspend fun cryptoRegister(
        email: String,
        associationKey: String,
    ): String =
        makeApiCall {
            apiService.cryptoRegister(CryptoRegisterRequest(email, associationKey))
        }.getOrThrow().customToken

    suspend fun cryptoLogin(
        email: String,
        data: String,
        signatureB64: String,
    ): String =
        makeApiCall {
            apiService.cryptoLogin(CryptoLoginRequest(email, data, signatureB64))
        }.getOrThrow().customToken

    suspend fun decryptRisk(encryptedEventString: String): DecryptRiskResponse =
        makeApiCall {
            riskApiService.decryptRisk(DecryptRiskRequest(encryptedEventString))
        }.getOrThrow()

    suspend fun emailLogin(email: String): KeyriResponse =
        makeApiCall {
            apiService.emailLogin(EmailLoginRequest(email))
        }.getOrThrow()

    suspend fun smsLogin(number: String): SmsLoginResponse {
        val fcmToken = Firebase.messaging.token.await()

        return makeApiCall {
            apiService.smsLogin(ReverseSmsLoginRequest(number.removePrefix(PHONE_PREFIX), fcmToken))
        }.getOrThrow()
    }

    suspend fun userRegister(
        name: String,
        email: String,
        number: String?,
    ): SmsLoginResponse =
        authWithFirebaseAndDoRequest(email) {
            apiService.userRegister(
                UserRegisterRequest(
                    name,
                    email,
                    number?.removePrefix(PHONE_PREFIX),
                ),
            )
        }

    suspend fun authWithToken(customToken: String): String =
        Firebase.auth
            .signInWithCustomToken(customToken)
            .await()
            .user
            ?.email
            ?: throw Exception("Failed to authenticate with custom token")

    suspend fun authWithFirebase(email: String) {
        val auth = Firebase.auth
        val hardcodedPassword = "HARDCODED_Pa$\$word"

        auth.signInWithEmailAndPassword(email, hardcodedPassword).await()
    }

    private suspend fun <T : Any> authWithFirebaseAndDoRequest(
        email: String,
        block: suspend () -> Response<T>,
    ): T {
        val auth = Firebase.auth
        val hardcodedPassword = "HARDCODED_Pa$\$word"

        val authTask = auth.createUserWithEmailAndPassword(email, hardcodedPassword)

        return callbackFlow {
            var callback: ((Result<T>) -> Unit)? = { trySend(it) }

            authTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        makeApiCall {
                            block()
                        }.onSuccess {
                            callback?.invoke(Result.success(it))
                        }.onFailure {
                            callback?.invoke(Result.failure(it))
                        }
                    }
                } else {
                    task.exception?.let {
                        callback?.invoke(Result.failure(it))
                    }
                }
            }

            awaitClose { callback = null }
        }.first().getOrThrow()
    }
}
