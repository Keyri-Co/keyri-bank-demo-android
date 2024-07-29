package com.keyri.androidFullExample.screens.verified

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.VerifyingState
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.timer

class VerifiedViewModel(
    val dataStore: DataStore<KeyriProfiles>,
    private val keyri: Keyri,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _loading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val loading = _loading.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()

    private val throwableScope =
        CoroutineExceptionHandler { _, throwable ->
            _errorMessage.value = throwable.message

            timer(initialDelay = 0L, period = 2_000L) {
                _errorMessage.value = null
            }
        }

    fun saveBiometricAuth(customToken: String) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            var currentProfileEmail = repository.authWithToken(customToken)

            dataStore.updateData { keyriProfiles ->
                if (keyriProfiles.currentProfile != null) {
                    currentProfileEmail = keyriProfiles.currentProfile
                }

                val mappedProfiles =
                    keyriProfiles.profiles.map {
                        if (currentProfileEmail == it.email) {
                            it.copy(
                                customToken = customToken,
                                emailVerifyState = VerifyingState.VERIFIED,
                            )
                        } else {
                            it
                        }
                    }

                keyriProfiles
                    .copy(currentProfile = currentProfileEmail, profiles = mappedProfiles)
            }

            val associationKey =
                keyri.getAssociationKey(currentProfileEmail).getOrNull()

            if (associationKey != null) {
                val data = System.currentTimeMillis().toString()
                val signature =
                    keyri.generateUserSignature(currentProfileEmail, data).getOrThrow()

                repository.cryptoLogin(currentProfileEmail, data, signature)
            } else {
                val generatedAssociationKey =
                    keyri.generateAssociationKey(currentProfileEmail).getOrThrow()

                repository.cryptoRegister(currentProfileEmail, generatedAssociationKey)
            }

            _loading.value = false
        }
    }
}
