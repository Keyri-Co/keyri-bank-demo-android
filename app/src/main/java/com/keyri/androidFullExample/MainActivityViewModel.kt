package com.keyri.androidFullExample

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.VerifyingState
import com.keyri.androidFullExample.routes.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivityViewModel(
    val dataStore: DataStore<KeyriProfiles>,
) : ViewModel() {
    private val _openScreen = MutableStateFlow<String?>(null)
    val openScreen = _openScreen.asStateFlow()

    fun checkStartScreen(data: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            var screenToOpen = Routes.WelcomeScreen.name

            data
                ?.toString()
                ?.replaceBefore("customToken=", "")
                ?.replace("customToken=", "")
                ?.let { customToken ->
                    dataStore.updateData { profiles ->
                        val mappedProfiles =
                            profiles.profiles.map {
                                if (it.email == profiles.currentProfile) {
                                    if (it.verifyState?.isVerificationDone() == true && it.biometricsSet) {
                                        screenToOpen = Routes.WelcomeScreen.name
                                    } else if (it.verifyState?.isVerificationDone() == true) {
                                        screenToOpen = Routes.VerifiedScreen.name
                                    }

                                    val newVerifyState =
                                        when (it.verifyState) {
                                            is VerifyingState.Email -> {
                                                val newState =
                                                    VerifyingState.Email(isVerified = true)

                                                newState.isVerifying = false

                                                screenToOpen = Routes.VerifiedScreen.name

                                                newState
                                            }

                                            is VerifyingState.EmailPhone -> {
                                                val newState =
                                                    it.verifyState.copy(emailVerified = true)

                                                screenToOpen =
                                                    if (newState.isVerificationDone()) {
                                                        newState.isVerifying = false

                                                        Routes.VerifiedScreen.name
                                                    } else {
                                                        "${Routes.VerifyScreen.name}?name=${it.name}?email=${it.email}&number=${it.phone}&isVerify=${it.isVerify}"
                                                    }

                                                newState
                                            }

                                            else -> {
                                                screenToOpen =
                                                    "${Routes.VerifyScreen.name}?name=${it.name}?email=${it.email}&number=${it.phone}&isVerify=${it.isVerify}"

                                                it.verifyState
                                            }
                                        }

                                    it.copy(verifyState = newVerifyState, customToken = customToken)
                                } else {
                                    it
                                }
                            }

                        profiles.copy(profiles = mappedProfiles).apply {
                            _openScreen.value = screenToOpen
                        }
                    }
                } ?: let {
                dataStore.data.collectLatest { profiles ->
                    profiles.profiles
                        .firstOrNull { it.email == profiles.currentProfile }
                        ?.let { profile ->
                            if (profile.verifyState?.isVerificationDone() == true && profile.biometricsSet) {
                                screenToOpen = Routes.WelcomeScreen.name
                            } else if (profile.verifyState?.isVerificationDone() == true && profile.customToken != null) {
                                screenToOpen = Routes.VerifiedScreen.name
                            }
                        }

                    _openScreen.value = screenToOpen
                }
            }
        }
    }
}
