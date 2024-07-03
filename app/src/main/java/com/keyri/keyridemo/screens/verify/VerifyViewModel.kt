package com.keyri.keyridemo.screens.verify

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.keyridemo.data.KeyriProfile
import com.keyri.keyridemo.data.KeyriProfiles
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.sec.fraud.event.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifyViewModel(private val keyri: Keyri, private val dataStore: DataStore<KeyriProfiles>) :
    ViewModel() {

    fun sendEvent(name: String?, email: String?, number: String?, onSent: () -> Unit) {
        if (email == null) return

        viewModelScope.launch(Dispatchers.IO) {
            if (keyri.getAssociationKey(email).getOrThrow() == null) {
                keyri.generateAssociationKey(email)
            }

            keyri.sendEvent(email, EventType.signup(), true)

            dataStore.updateData {
                val mappedProfiles =
                    if (it.profiles.any { profile -> profile.email == email }) it.profiles else {
                        it.profiles + KeyriProfile(name, email, number, false)
                    }

                it.copy(profiles = mappedProfiles)
            }

            withContext(Dispatchers.Main) {
                onSent()
            }
        }
    }
}
