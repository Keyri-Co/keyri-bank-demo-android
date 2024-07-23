package com.keyri.androidFullExample.repositories

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.keyri.androidFullExample.services.ApiService
import com.keyri.androidFullExample.services.entities.requests.CryptoLoginRequest
import com.keyri.androidFullExample.services.entities.requests.CryptoRegisterRequest
import com.keyri.androidFullExample.services.entities.requests.DecryptRiskRequest
import com.keyri.androidFullExample.services.entities.requests.EmailLoginRequest
import com.keyri.androidFullExample.services.entities.requests.ReverseSmsLoginRequest
import com.keyri.androidFullExample.services.entities.requests.UserInformationResponse
import com.keyri.androidFullExample.services.entities.requests.UserRegisterRequest
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
) {
    suspend fun cryptoRegister(email: String, associationKey: String): String {
        return makeApiCall {
            apiService.cryptoRegister(CryptoRegisterRequest(email, associationKey))
        }.getOrThrow().customToken
    }

    suspend fun cryptoLogin(email: String, data: String, signatureB64: String): String {
        return makeApiCall {
            apiService.cryptoLogin(CryptoLoginRequest(email, data, signatureB64))
        }.getOrThrow().customToken
    }

    suspend fun decryptRisk(encryptedEventString: String): String {
        return makeApiCall {
            apiService.decryptRisk(DecryptRiskRequest(encryptedEventString))
        }.getOrThrow().customToken
    }

    suspend fun emailLogin(
        isVerify: Boolean,
        email: String,
    ): KeyriResponse =
        authWithFirebaseAndDoRequest(isVerify, email) {
            apiService.emailLogin(EmailLoginRequest(email))
        }

    suspend fun smsLogin(
        isVerify: Boolean,
        email: String,
        number: String,
    ): SmsLoginResponse {
        val fcmToken = Firebase.messaging.token.await()

        return authWithFirebaseAndDoRequest(isVerify, email) {
            apiService.smsLogin(ReverseSmsLoginRequest(number.removePrefix(PHONE_PREFIX), fcmToken))
        }
    }

    suspend fun userRegister(
        isVerify: Boolean,
        name: String,
        email: String,
        number: String?,
    ): SmsLoginResponse = authWithFirebaseAndDoRequest(isVerify, email) {
        apiService.userRegister(
            UserRegisterRequest(
                name,
                email,
                number?.removePrefix(PHONE_PREFIX)
            ),
        )
    }

    suspend fun getUserInformation(email: String): UserInformationResponse =
        makeApiCall {
            apiService.getUserInformation(EmailLoginRequest(email))
        }.getOrThrow()

    suspend fun authWithToken(customToken: String): String =
        callbackFlow {
            var callback: ((String) -> Unit)? = { trySend(it) }
            Firebase.auth
                .signInWithCustomToken(customToken)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.email?.let {
                            callback?.invoke(it)
                        }
                    } else {
                        task.exception?.let {
                            throw it
                        }
                    }
                }

            awaitClose { callback = null }
        }.first()

    private suspend fun <T : Any> authWithFirebaseAndDoRequest(
        isVerify: Boolean,
        email: String,
        block: suspend () -> Response<T>,
    ): T {
        val auth = Firebase.auth

        val hardcodedPassword = "HARDCODED_Pa$\$word"

        val authTask =
            if (isVerify) {
                auth.createUserWithEmailAndPassword(email, hardcodedPassword)
            } else {
                auth.signInWithEmailAndPassword(email, hardcodedPassword)
            }

        return callbackFlow {
            var callback: ((T) -> Unit)? = { trySend(it) }

            authTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        makeApiCall {
                            block()
                        }.getOrThrow().let {
                            callback?.invoke(it)
                        }
                    }
                } else {
                    task.exception?.let {
                        throw it
                    }
                }
            }

            awaitClose { callback = null }
        }.first()
    }
}
