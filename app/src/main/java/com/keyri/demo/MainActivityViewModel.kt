package com.keyri.demo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val dataStore: DataStore<Preferences>,
    private val keyri: Keyri
) : ViewModel() {

    private val _keyriAccounts = MutableStateFlow<Map<String, String>?>(null)
    val keyriAccounts = _keyriAccounts.asStateFlow()

    fun checkKeyriAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: Add processing failure result
            keyri.listUniqueAccounts().onSuccess {
                _keyriAccounts.value = it
            }
        }
    }

    fun checkIsBiometricAuthSet(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("isBiometricAuthSet")] ?: false
        }
    }
}
