package com.keyri.androidFullExample.screens.verify

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.keyri.androidFullExample.data.KeyriProfile
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.VerifyingState
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyri.androidFullExample.services.entities.responses.SmsLoginResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class VerifyViewModel(
    val dataStore: DataStore<KeyriProfiles>,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
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
        isVerify: Boolean,
        name: String,
        email: String,
        number: String?,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            userRegister(isVerify, name, email, number)
            repository.emailLogin(email)

            updateVerifyState(email, null)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun smsLogin(
        isVerify: Boolean,
        name: String,
        email: String,
        phone: String,
        onSuccess: (SmsLoginResponse) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            userRegister(isVerify, name, email, phone)

            val result = repository.smsLogin(phone)

            updateVerifyState(null, phone)

            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        }
    }

    fun smsAndEmailLogin(
        isVerify: Boolean,
        name: String,
        email: String,
        phone: String,
        onSuccess: (SmsLoginResponse) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            userRegister(isVerify, name, email, phone)
            repository.emailLogin(email)

            val result = repository.smsLogin(phone)

            updateVerifyState(email, phone)

            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        }
    }

    fun cancelVerify(onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            dataStore.updateData { keyriProfiles ->
                val mappedProfiles =
                    keyriProfiles.profiles.map {
                        if (keyriProfiles.currentProfile == it.email) {
                            it.copy(verifyState = null)
                        } else {
                            it
                        }
                    }

                keyriProfiles.copy(
                    currentProfile = null,
                    profiles = mappedProfiles,
                )
            }

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    private suspend fun userRegister(
        isVerify: Boolean,
        name: String,
        email: String,
        phone: String?,
    ) {
        if (isVerify) {
            try {
                repository.userRegister(name, email, phone)
            } catch (e: Exception) {
                if (e.message?.contains("The email address is already in use by another account") == true) {
                    repository.authWithFirebase(email)
                }
            }
        } else {
            repository.authWithFirebase(email)
        }

        createEmptyKeyriAccount(name, email, phone, isVerify)
    }

    private suspend fun updateVerifyState(
        email: String?,
        phone: String?,
    ) {
        dataStore.updateData { keyriProfiles ->
            val mappedProfiles =
                keyriProfiles.profiles.map {
                    if (keyriProfiles.currentProfile == it.email) {
                        when {
                            email != null && phone != null -> {
                                val verifyState =
                                    VerifyingState.EmailPhone(
                                        emailVerified = false,
                                        phoneVerified = false,
                                    )
                                verifyState.isVerifying = true

                                it.copy(
                                    verifyState = verifyState,
                                )
                            }

                            email != null && phone == null -> {
                                val verifyState = VerifyingState.Email(isVerified = false)
                                verifyState.isVerifying = true

                                it.copy(verifyState = verifyState)
                            }

                            email == null && phone != null -> {
                                val verifyState = VerifyingState.Phone(isVerified = false)
                                verifyState.isVerifying = true

                                it.copy(verifyState = verifyState)
                            }

                            else -> it
                        }
                    } else {
                        it
                    }
                }

            keyriProfiles.copy(profiles = mappedProfiles)
        }
    }

    private suspend fun createEmptyKeyriAccount(
        name: String,
        email: String,
        phone: String?,
        isVerify: Boolean,
    ) {
        dataStore.updateData { keyriProfiles ->
            val mappedProfiles =
                if (keyriProfiles.profiles.any { it.email == email }) {
                    keyriProfiles.profiles.map {
                        if (keyriProfiles.currentProfile == email) {
                            it.copy(
                                name = name,
                                email = email,
                                phone = phone,
                                isVerify = isVerify,
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
                            phone = phone,
                            isVerify = isVerify,
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
}
