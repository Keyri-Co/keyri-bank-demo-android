package com.keyri.demo.screens.verified

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.demo.data.KeyriProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VerifiedViewModel(private val dataStore: DataStore<KeyriProfiles>) : ViewModel() {

    fun saveBiometricAuth() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.updateData { keyriProfiles ->
                val mappedProfiles = keyriProfiles.profiles.map {
                    if (keyriProfiles.currentProfile == it.name) {
                        it.copy(biometricAuthEnabled = true)
                    } else {
                        it
                    }
                }

                keyriProfiles.copy(profiles = mappedProfiles)
            }
        }
    }
}
