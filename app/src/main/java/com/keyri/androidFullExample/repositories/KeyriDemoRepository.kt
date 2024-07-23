package com.keyri.androidFullExample.repositories

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.keyri.androidFullExample.services.ApiService
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
import retrofit2.Response

class KeyriDemoRepository(private val apiService: ApiService) {

    suspend fun emailLogin(isVerify: Boolean, email: String): KeyriResponse {
        return authWithFirebaseAndDoRequest(isVerify, email) {
            apiService.emailLogin(EmailLoginRequest(email))
        }
    }

    suspend fun smsLogin(isVerify: Boolean, email: String, number: String): SmsLoginResponse {
        return authWithFirebaseAndDoRequest(isVerify, email) {
            apiService.smsLogin(ReverseSmsLoginRequest(number.removePrefix(PHONE_PREFIX)))
        }
    }

    suspend fun userRegister(
        isVerify: Boolean,
        name: String,
        email: String,
        number: String?,
    ): SmsLoginResponse {
        return authWithFirebaseAndDoRequest(isVerify, email) {
            apiService.userRegister(
                UserRegisterRequest(
                    name,
                    email,
                    number?.removePrefix(PHONE_PREFIX)
                )
            )
        }
    }

    suspend fun getUserInformation(email: String): UserInformationResponse = makeApiCall {
        apiService.getUserInformation(EmailLoginRequest(email))
    }.getOrThrow()

    suspend fun authWithToken(customToken: String): String {
        return callbackFlow {
            var callback: ((String) -> Unit)? = { trySend(it) }
            Firebase.auth.signInWithCustomToken(customToken)
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
    }

    private suspend fun <T : Any> authWithFirebaseAndDoRequest(
        isVerify: Boolean,
        email: String,
        block: suspend () -> Response<T>
    ): T {
        val auth = Firebase.auth

        val hardcodedPassword = "HARDCODED_Pa$\$word"

        val authTask = if (isVerify) {
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
