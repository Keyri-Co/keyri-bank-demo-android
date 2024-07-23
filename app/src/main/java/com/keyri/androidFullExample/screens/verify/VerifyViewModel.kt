package com.keyri.androidFullExample.screens.verify

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfile
import com.keyri.androidFullExample.data.KeyriProfiles
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
    private val dataStore: DataStore<KeyriProfiles>,
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

    fun userRegister(
        isVerify: Boolean,
        name: String,
        email: String,
        phone: String?,
        onSuccess: (SmsLoginResponse) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            val result = repository.userRegister(isVerify, name, email, phone)

            createEmptyKeyriAccount(name, email, phone, isVerify)

            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        }
    }

    fun emailLogin(
        isVerify: Boolean,
        name: String,
        email: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            repository.emailLogin(isVerify, email)

            createEmptyKeyriAccount(name, email, null, isVerify)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun smsLogin(
        isVerify: Boolean,
        name: String,
        email: String,
        number: String,
        onSuccess: (SmsLoginResponse) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            val result = repository.smsLogin(isVerify, email, number)

            createEmptyKeyriAccount(name, email, null, isVerify)

            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        }
    }

    private suspend fun createEmptyKeyriAccount(
        name: String,
        email: String,
        phone: String?,
        isVerify: Boolean,
    ) {
        dataStore.updateData { keyriProfiles ->
            val newProfile = KeyriProfile(name, email, phone, isVerify, false, null, false)

            keyriProfiles.copy(
                currentProfile = email,
                profiles = keyriProfiles.profiles + newProfile,
            )
        }
    }
}
