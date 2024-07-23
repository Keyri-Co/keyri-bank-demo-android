package com.keyri.androidFullExample.screens.verified

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifiedViewModel(
    private val dataStore: DataStore<KeyriProfiles>,
) : ViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    fun saveBiometricAuth(
        currentProfile: String,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.updateData { keyriProfiles ->
                val mappedProfiles =
                    keyriProfiles.profiles.map {
                        if (currentProfile == it.name) {
                            it.copy(biometricAuthEnabled = true)
                        } else {
                            it
                        }
                    }

                keyriProfiles
                    .copy(currentProfile = currentProfile, profiles = mappedProfiles)
                    .apply {
                        withContext(Dispatchers.Main) {
                            onDone()
                        }
                    }
            }
        }
    }

    // TODO: Do this after opening screen
    //    fun sendEvent(
//        name: String?,
//        email: String?,
//        number: String?,
//        onSuccess: () -> Unit,
//    ) {
//        if (email == null) return
//
//        viewModelScope.launch(throwableScope) {
//            if (keyri.getAssociationKey(email).getOrThrow() == null) {
//                keyri.generateAssociationKey(email)
//            }
//
//            keyri.sendEvent(email, EventType.signup(), true)
//
//            dataStore.updateData {
//                val mappedProfiles =
//                    if (it.profiles.any { profile -> profile.email == email }) {
//                        it.profiles
//                    } else {
//                        it.profiles + KeyriProfile(name, email, number, false)
//                    }
//
//                it.copy(profiles = mappedProfiles)
//            }
//
//            withContext(Dispatchers.Main) {
//                onSuccess()
//            }
//        }
//    }
}
