package com.keyri.androidFullExample.screens.verified

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfile
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.timer

class VerifiedViewModel(
    private val keyri: Keyri,
    private val repository: KeyriDemoRepository,
    private val dataStore: DataStore<KeyriProfiles>,
) : ViewModel() {
    private val _loading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _currentProfile = MutableStateFlow<KeyriProfile?>(null)
    val loading = _loading.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()
    val currentProfile = _currentProfile.asStateFlow()

    private val throwableScope =
        CoroutineExceptionHandler { _, throwable ->
            _errorMessage.value = throwable.message

            timer(initialDelay = 1_000L, period = 1_000L) {
                _errorMessage.value = null
            }
        }

    // TODO: Can't see crypto-auth logs on login (with one account)

    fun saveBiometricAuth(customToken: String) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            val currentProfileEmail = repository.authWithToken(customToken)

            // TODO: 0. Auth with firebase
            // TODO: 1. Check current profile
            // TODO: 2. If current profile is null - get

            // TODO: If no Keyri and DataStore profiles on device -> create new one? and call getUserInformation()
            // This repository.getUserInformation(currentProfileEmail)

            dataStore.updateData { keyriProfiles ->
                val mappedProfiles =
                    keyriProfiles.profiles.map {
                        if (currentProfileEmail == it.email) {
                            it
                                .copy(
                                    customToken = customToken,
                                    isVerified = true,
                                ).apply {
                                    _currentProfile.value = it
                                }
                        } else {
                            it
                        }
                    }

                keyriProfiles
                    .copy(currentProfile = currentProfileEmail, profiles = mappedProfiles)
            }

            val associationKey =
                keyri.getAssociationKey(currentProfileEmail).getOrNull()
                    ?: keyri.generateAssociationKey(currentProfileEmail).getOrThrow()

            _currentProfile.value?.let { profile ->
                if (profile.isVerify) {
                    repository.cryptoRegister(currentProfileEmail, associationKey)
                } else {
                    val data = System.currentTimeMillis().toString()
                    val signature =
                        keyri.generateUserSignature(currentProfileEmail, data).getOrThrow()

                    repository.cryptoLogin(profile.email, data, signature)
                }
            }

            _loading.value = false
        }
    }
}
