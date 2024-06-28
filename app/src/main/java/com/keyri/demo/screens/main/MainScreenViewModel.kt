package com.keyri.demo.screens.main

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.demo.data.KeyriProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainScreenViewModel(private val dataStore: DataStore<KeyriProfiles>) : ViewModel() {

    fun getCurrentProfile() {

    }

    fun logout(callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.updateData { keyriProfiles ->
                keyriProfiles.copy(currentProfile = null).apply {
                    callback()
                }
            }
        }
    }
}
