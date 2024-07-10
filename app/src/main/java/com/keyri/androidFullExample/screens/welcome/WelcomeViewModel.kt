package com.keyri.androidFullExample.screens.welcome

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeViewModel(private val dataStore: DataStore<KeyriProfiles>, private val keyri: Keyri) :
    ViewModel() {

    private val _keyriAccounts = MutableStateFlow(KeyriProfiles(null, emptyList()))
    val keyriAccounts = _keyriAccounts.asStateFlow()

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
