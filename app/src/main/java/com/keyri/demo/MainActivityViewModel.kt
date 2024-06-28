package com.keyri.demo

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.demo.data.KeyriProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivityViewModel(private val dataStore: DataStore<KeyriProfiles>) : ViewModel() {

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
}
