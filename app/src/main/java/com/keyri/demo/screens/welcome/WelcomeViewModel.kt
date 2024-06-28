package com.keyri.demo.screens.welcome

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.demo.data.KeyriProfiles
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeViewModel(private val dataStore: DataStore<KeyriProfiles>, private val keyri: Keyri) :
    ViewModel() {

    fun setCurrentProfile(currentProfile: String?) {
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

                withContext(Dispatchers.Main) {
                    callback()
                }
            }
        }
    }
}
