package com.keyri.androidFullExample.screens.login

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.keyri.androidFullExample.data.KeyriProfile
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.VerifyingState
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class LoginViewModel(
    private val dataStore: DataStore<KeyriProfiles>,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val loading = _loading.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()

    private val throwableScope =
        CoroutineExceptionHandler { _, throwable ->
            _errorMessage.value = throwable.message

            throwable.let { e ->
                Firebase.crashlytics.recordException(e)
            }

            timer(initialDelay = 0L, period = 2_000L) {
                _errorMessage.value = null
            }
        }

    fun emailLogin(
        email: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            _loading.value = true

            userRegister(email.split("@").first().toString(), email)
            repository.emailLogin(email)
            updateVerifyState(email)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    private suspend fun userRegister(
        name: String,
        email: String,
    ) {
        repository.authWithFirebase(email)
        createEmptyKeyriAccount(name, email)
    }

    private suspend fun updateVerifyState(email: String) {
        dataStore.updateData { keyriProfiles ->
            val mappedProfiles =
                keyriProfiles.profiles.map {
                    if (email == it.email) {
                        val verifyState = VerifyingState.Email(isVerified = false)

                        verifyState.isVerifying = true

                        it.copy(verifyState = verifyState)
                    } else {
                        it
                    }
                }

            keyriProfiles.copy(currentProfile = email, profiles = mappedProfiles)
        }
    }

    private suspend fun createEmptyKeyriAccount(
        name: String,
        email: String,
    ) {
        dataStore.updateData { keyriProfiles ->
            val mappedProfiles =
                if (keyriProfiles.profiles.any { it.email == email }) {
                    keyriProfiles.profiles.map {
                        if (keyriProfiles.currentProfile == email) {
                            it.copy(
                                name = name,
                                email = email,
                                phone = null,
                                isVerify = false,
                                verifyState = null,
                                customToken = null,
                                biometricsSet = false,
                            )
                        } else {
                            it
                        }
                    }
                } else {
                    val newProfile =
                        KeyriProfile(
                            name = name,
                            email = email,
                            phone = null,
                            isVerify = false,
                            verifyState = null,
                            customToken = null,
                            associationKey = null,
                            biometricsSet = false,
                        )

                    keyriProfiles.profiles + newProfile
                }

            keyriProfiles.copy(currentProfile = email, profiles = mappedProfiles)
        }
    }

    fun stopLoading() {
        _loading.value = false
    }
}
