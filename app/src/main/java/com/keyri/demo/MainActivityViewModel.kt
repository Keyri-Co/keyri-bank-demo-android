package com.keyri.demo

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val dataStore: DataStore<Preferences>,
    private val keyri: Keyri
) : ViewModel() {

    private val _keyriAccounts = MutableStateFlow<Map<String, String>?>(null)
    private val _isBiometricAuthSet = MutableStateFlow(false)
    val keyriAccounts =_keyriAccounts.asStateFlow()
    val isBiometricAuthSet = _isBiometricAuthSet.asStateFlow()

    fun checkKeyriAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            keyri.listUniqueAccounts().onSuccess {
                _keyriAccounts.value = it

                Log.e("OK", it.map { it.key + ", " }.toString())
            }
                // TODO: Remove it and logs
                .onFailure {
                Log.e("FAILURE", it.message ?: "")
            }
        }
    }

    fun checkIsBiometricAuthSet() {
        dataStore.data.map { preferences ->
            _isBiometricAuthSet.value =
                preferences[booleanPreferencesKey("isBiometricAuthSet")] ?: false
        }
    }
}
