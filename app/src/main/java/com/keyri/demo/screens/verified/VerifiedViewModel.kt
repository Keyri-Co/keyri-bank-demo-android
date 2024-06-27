package com.keyri.demo.screens.verified

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VerifiedViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {

    fun saveBiometricAuth() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey("isBiometricAuthSet")] = true
            }
        }
    }
}
