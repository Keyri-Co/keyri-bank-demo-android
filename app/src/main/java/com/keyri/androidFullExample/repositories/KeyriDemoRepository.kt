package com.keyri.androidFullExample.repositories

import androidx.datastore.core.DataStore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.services.ApiService
import com.keyri.androidFullExample.services.entities.requests.EmailLoginRequest
import com.keyri.androidFullExample.services.entities.requests.ReverseSmsLoginRequest
import com.keyri.androidFullExample.services.entities.requests.UserInformationResponse
import com.keyri.androidFullExample.services.entities.requests.UserRegisterRequest
import com.keyri.androidFullExample.services.entities.responses.KeyriResponse
import com.keyri.androidFullExample.services.makeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class KeyriDemoRepository(
    private val apiService: ApiService,
    private val dataStore: DataStore<KeyriProfiles>
) {
    // TODO: Save tokens to database?

    suspend fun emailLogin(email: String): KeyriResponse =
        makeApiCall { apiService.emailLogin(EmailLoginRequest(email)) }.getOrThrow()

    suspend fun smsLogin(number: String): KeyriResponse =
        makeApiCall { apiService.smsLogin(ReverseSmsLoginRequest(number)) }.getOrThrow()

    suspend fun userRegister(
        isVerify: Boolean,
        name: String,
        email: String,
        number: String?,
    ): KeyriResponse {
        val auth = Firebase.auth

        val hardcodedPassword = "HARDCODED_Pa$\$word"

        val authTask = if (isVerify) {
            auth.createUserWithEmailAndPassword(email, hardcodedPassword)
        } else {
            auth.signInWithEmailAndPassword(email, hardcodedPassword)
        }

        return callbackFlow {
            var callback: ((KeyriResponse) -> Unit)? = { trySend(it) }

            authTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        makeApiCall {
                            apiService.userRegister(UserRegisterRequest(name, email, number))
                        }.getOrThrow().let {
                            callback?.invoke(it)
                        }
                    }
                }
            }

            awaitClose { callback = null }
        }.first()
    }

    suspend fun getUserInformation(email: String): UserInformationResponse = makeApiCall {
        apiService.getUserInformation(EmailLoginRequest(email))
    }.getOrThrow()
}
