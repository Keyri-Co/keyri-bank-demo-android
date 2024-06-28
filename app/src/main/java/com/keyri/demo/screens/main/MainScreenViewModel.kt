package com.keyri.demo.screens.main

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.demo.data.KeyriProfile
import com.keyri.demo.data.KeyriProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class MainScreenViewModel(private val dataStore: DataStore<KeyriProfiles>) : ViewModel() {

    private val _currentProfile = MutableStateFlow<KeyriProfile?>(null)
    val currentProfile = _currentProfile.asStateFlow()

    init {
        getCurrentProfile()
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

    private fun getCurrentProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.data.mapNotNull { keyriProfiles ->
                keyriProfiles.profiles.firstOrNull { it.name == keyriProfiles.currentProfile }
            }.collectLatest {
                _currentProfile.value = it
            }
        }
    }
}
