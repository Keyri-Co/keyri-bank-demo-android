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
        Dispatchers.IO +
            CoroutineExceptionHandler { _, throwable ->
                _errorMessage.value = throwable.message

                timer(initialDelay = 1_000L, period = 1_000L) {
                    _errorMessage.value = null
                }
            }

    fun saveBiometricAuth(customToken: String) {
        viewModelScope.launch(throwableScope) {
            val currentProfileEmail = repository.authWithToken(customToken)

            dataStore.updateData { keyriProfiles ->
                val mappedProfiles =
                    keyriProfiles.profiles.map {
                        if (currentProfileEmail == it.name) {
                            val mappedProfile =
                                it.copy(
                                    customToken = customToken,
                                    isVerified = true,
                                    biometricAuthEnabled = true,
                                )

                            _currentProfile.value = mappedProfile

                            mappedProfile
                        } else {
                            it
                        }
                    }

                keyriProfiles
                    .copy(currentProfile = currentProfileEmail, profiles = mappedProfiles)
                    .apply {
                        _loading.value = true
                    }
            }

            val associationKey =
                keyri.getAssociationKey(currentProfileEmail).getOrNull()
                    ?: keyri.generateAssociationKey(currentProfileEmail).getOrThrow()

            currentProfile.value?.let { profile ->
                if (profile.isVerify) {
                    repository.cryptoRegister(currentProfileEmail, associationKey)
                } else {
                    val data = System.currentTimeMillis().toString()
                    val signature =
                        keyri.generateUserSignature(currentProfileEmail, data).getOrThrow()

                    repository.cryptoLogin(profile.email, data, signature)
                }
            }
        }
    }
}
