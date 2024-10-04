package com.keyri.androidFullExample.screens.requestsent

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import com.keyri.androidFullExample.data.KeyriProfiles

class RequestSentViewModel(
    private val dataStore: DataStore<KeyriProfiles>,
) : ViewModel() {
    suspend fun setCurrentAccount(email: String) {
        dataStore.updateData { keyriProfiles ->
            keyriProfiles.copy(currentProfile = email)
        }
    }
}
