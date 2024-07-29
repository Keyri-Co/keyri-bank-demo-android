package com.keyri.androidFullExample.screens.verify

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

            timer(initialDelay = 1_000L, period = 1_000L) {
                _errorMessage.value = null
            }
        }

    fun emailLogin(
        isVerify: Boolean,
        name: String,
        email: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            userRegister(isVerify, name, email, null)
            repository.emailLogin(email)

            updateVerifyState(email, null)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    // TODO: Undo here

    fun smsLogin(
        isVerify: Boolean,
        name: String,
        email: String,
        number: String,
        onSuccess: (SmsLoginResponse) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            userRegister(isVerify, name, email, number)

            val result = repository.smsLogin(email)
//            val result = repository.smsLogin(number)

            updateVerifyState(null, number)

//            withContext(Dispatchers.Main) {
//                onSuccess(result)
//            }
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

            val result = repository.smsLogin(email)
//            val result = repository.smsLogin(phone)

            updateVerifyState(email, phone)

//            withContext(Dispatchers.Main) {
//                onSuccess(result)
//            }
        }
    }

    private suspend fun userRegister(
        isVerify: Boolean,
        name: String,
        email: String,
        phone: String?,
    ) {
        if (isVerify) {
            repository.userRegister(name, email, phone)
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
                            email != null && phone != null ->
                                it.copy(
                                    emailVerifyState = VerifyingState.VERIFYING,
                                    phoneVerifyState = VerifyingState.VERIFYING,
                                )

                            email != null && phone == null ->
                                it.copy(
                                    emailVerifyState = VerifyingState.VERIFYING,
                                )

                            email == null && phone != null ->
                                it.copy(
                                    phoneVerifyState = VerifyingState.VERIFYING,
                                )

                            else -> it
                        }
                    } else {
                        it
                    }
                }

            keyriProfiles.copy(
                currentProfile = email,
                profiles = mappedProfiles,
            )
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
                                emailVerifyState = VerifyingState.NOT_VERIFIED,
                                phoneVerifyState = VerifyingState.NOT_VERIFIED,
                                customToken = null,
                            )
                        } else {
                            it
                        }
                    }
                } else {
                    val newProfile =
                        KeyriProfile(
                            name,
                            email,
                            phone,
                            isVerify,
                            emailVerifyState = VerifyingState.NOT_VERIFIED,
                            phoneVerifyState = VerifyingState.NOT_VERIFIED,
                            null,
                        )

                    keyriProfiles.profiles + newProfile
                }

            keyriProfiles.copy(
                currentProfile = email,
                profiles = mappedProfiles,
            )
        }
    }
}
