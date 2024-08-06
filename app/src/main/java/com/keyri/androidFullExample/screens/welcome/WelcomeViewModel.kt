package com.keyri.androidFullExample.screens.welcome

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class WelcomeViewModel(
    val dataStore: DataStore<KeyriProfiles>,
    private val keyri: Keyri,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _blockBiometricPrompt = MutableStateFlow(false)
    val errorMessage = _errorMessage.asStateFlow()
    val blockBiometricPrompt = _blockBiometricPrompt.asStateFlow()

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

    fun cryptoLogin(
        currentProfile: String,
        onResult: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            val data = System.currentTimeMillis().toString()
            val signature = keyri.generateUserSignature(currentProfile, data).getOrThrow()

            repository.cryptoLogin(currentProfile, data, signature)

            dataStore.updateData { keyriProfiles ->
                val mappedProfiles =
                    keyriProfiles.profiles.map {
                        if (it.email == currentProfile) {
                            it.copy(isVerify = false)
                        } else {
                            it
                        }
                    }

                _blockBiometricPrompt.value = true

                keyriProfiles.copy(currentProfile = currentProfile, profiles = mappedProfiles)
            }

            withContext(Dispatchers.Main) {
                onResult()
            }
        }
    }

    fun removeAllAccounts(callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            keyri.listUniqueAccounts().onSuccess { accounts ->
                accounts.forEach { (name, _) ->
                    keyri.removeAssociationKey(name)
                }

                dataStore.updateData { keyriProfiles ->
                    keyriProfiles.copy(currentProfile = null, profiles = listOf())
                }

                withContext(Dispatchers.Main) {
                    callback()
                }
            }
        }
    }
}
