package com.keyri.androidFullExample.screens.verified

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifiedViewModel(private val dataStore: DataStore<KeyriProfiles>) : ViewModel() {

    fun saveBiometricAuth(currentProfile: String, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.updateData { keyriProfiles ->
                val mappedProfiles = keyriProfiles.profiles.map {
                    if (currentProfile == it.name) {
                        it.copy(biometricAuthEnabled = true)
                    } else {
                        it
                    }
                }

                keyriProfiles.copy(currentProfile = currentProfile, profiles = mappedProfiles)
                    .apply {
                        withContext(Dispatchers.Main) {
                            onDone()
                        }
                    }
            }
        }
    }
}
