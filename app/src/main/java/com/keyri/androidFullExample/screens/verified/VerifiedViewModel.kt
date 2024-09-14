package com.keyri.androidFullExample.screens.verified

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.VerifyingState
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class VerifiedViewModel(
    val dataStore: DataStore<KeyriProfiles>,
    private val keyri: Keyri,
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

    fun saveBiometricAuth(onResult: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            _loading.value = true

            val allProfiles = dataStore.data.first()

            val customToken =
                allProfiles.profiles.firstOrNull { it.email == allProfiles.currentProfile }?.customToken
                    ?: throw IllegalStateException("CustomToken shouldn't be null")

            var currentProfileEmail = repository.authWithToken(customToken)

            dataStore.updateData { keyriProfiles ->
                if (keyriProfiles.currentProfile != null) {
                    currentProfileEmail = keyriProfiles.currentProfile
                }

                val mappedProfiles =
                    keyriProfiles.profiles.map {
                        if (currentProfileEmail == it.email) {
                            val newVerifyState =
                                when (it.verifyState) {
                                    is VerifyingState.Email -> it.verifyState.copy(isVerified = true)
                                    is VerifyingState.Phone -> it.verifyState.copy(isVerified = true)
                                    is VerifyingState.EmailPhone ->
                                        it.verifyState.copy(
                                            emailVerified = true,
                                            phoneVerified = true,
                                        )

                                    else -> null
                                }

                            var associationKey =
                                keyri.getAssociationKey(currentProfileEmail).getOrNull()

                            if (it.associationKey == null) {
                                if (associationKey == null) {
                                    associationKey = keyri.generateAssociationKey(currentProfileEmail).getOrThrow()
                                }

                                repository.cryptoRegister(currentProfileEmail, associationKey)
                            } else {
                                if (associationKey != null) {
                                    val data = System.currentTimeMillis().toString()
                                    val signature = keyri.generateUserSignature(currentProfileEmail, data).getOrThrow()

                                    repository.cryptoLogin(currentProfileEmail, data, signature)
                                } else {
                                    associationKey = keyri.generateAssociationKey(currentProfileEmail).getOrThrow()

                                    repository.cryptoRegister(currentProfileEmail, associationKey)
                                }
                            }

                            it.copy(
                                customToken = customToken,
                                associationKey = associationKey,
                                verifyState = newVerifyState,
                                biometricsSet = true,
                            )
                        } else {
                            it
                        }
                    }

                keyriProfiles
                    .copy(currentProfile = currentProfileEmail, profiles = mappedProfiles)
                    .apply {
                        _loading.value = false

                        withContext(Dispatchers.Main) {
                            onResult()
                        }
                    }
            }
        }
    }
}
