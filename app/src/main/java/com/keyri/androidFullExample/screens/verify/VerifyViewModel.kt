package com.keyri.androidFullExample.screens.verify

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfile
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.sec.fraud.event.EventType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class VerifyViewModel(
    private val keyri: Keyri,
    private val dataStore: DataStore<KeyriProfiles>,
    private val repository: KeyriDemoRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val throwableScope = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        _errorMessage.value = throwable.message

        timer(initialDelay = 1_000L, period = 1_000L) {
            _errorMessage.value = null
        }
    }

    fun sendEvent(name: String?, email: String?, number: String?, onSuccess: () -> Unit) {
        if (email == null) return

        viewModelScope.launch(throwableScope) {
            if (keyri.getAssociationKey(email).getOrThrow() == null) {
                keyri.generateAssociationKey(email)
            }

            keyri.sendEvent(email, EventType.signup(), true)

            dataStore.updateData {
                val mappedProfiles =
                    if (it.profiles.any { profile -> profile.email == email }) it.profiles else {
                        it.profiles + KeyriProfile(name, email, number, false)
                    }

                it.copy(profiles = mappedProfiles)
            }

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun cryptoRegister(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch(throwableScope) {
            val associationKey = keyri.getAssociationKey(email).getOrNull()
                ?: keyri.generateAssociationKey(email).getOrThrow()

            repository.cryptoRegister(email, associationKey)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun cryptoLogin(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch(throwableScope) {
            val data = System.currentTimeMillis().toString()
            val signatureB64 = keyri.generateUserSignature(email, data).getOrThrow()

            repository.cryptoLogin(email, data, signatureB64)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun emailLogin(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch(throwableScope) {
            repository.emailLogin(email)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun smsLogin(number: String, onSuccess: () -> Unit) {
        viewModelScope.launch(throwableScope) {
            repository.smsLogin(number)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
