package com.keyri.androidFullExample.screens.welcome

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class WelcomeViewModel(
    private val dataStore: DataStore<KeyriProfiles>,
    private val keyri: Keyri,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _keyriAccounts = MutableStateFlow(KeyriProfiles(null, emptyList()))
    private val _errorMessage = MutableStateFlow<String?>(null)
    val keyriAccounts = _keyriAccounts.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()

    private val throwableScope =
        CoroutineExceptionHandler { _, throwable ->
            _errorMessage.value = throwable.message

            timer(initialDelay = 1_000L, period = 1_000L) {
                _errorMessage.value = null
            }
        }

    init {
        checkKeyriAccounts()
    }

    fun checkKeyriAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.data.collectLatest {
                _keyriAccounts.value = it
            }
        }
    }

    fun setCurrentProfile(currentProfile: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.updateData { keyriProfiles ->
                val mappedProfiles =
                    keyriProfiles.profiles.map {
                        if (currentProfile == it.name) {
                            it.copy(biometricAuthEnabled = true)
                        } else {
                            it
                        }
                    }

                keyriProfiles.copy(currentProfile = currentProfile, profiles = mappedProfiles)
            }
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

            withContext(Dispatchers.Main) {
                onResult()
            }
        }
    }

    fun removeAllAccounts(callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            keyri.listUniqueAccounts().onSuccess { accounts ->
                accounts.forEach { (name, _) ->
                    keyri.removeAssociationKey(name)
                }

                dataStore.updateData { keyriProfiles ->
                    keyriProfiles.copy(currentProfile = null, profiles = listOf())
                }

                checkKeyriAccounts()

                withContext(Dispatchers.Main) {
                    callback()
                }
            }
        }
    }
}
