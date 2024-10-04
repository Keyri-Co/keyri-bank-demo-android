package com.keyri.androidFullExample

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.data.VerifyingState
import com.keyri.androidFullExample.routes.Routes
import com.keyri.androidFullExample.utils.CUSTOM_TOKEN_PARAM
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class MainActivityViewModel(
    val dataStore: DataStore<KeyriProfiles>,
    val keyri: Keyri,
) : ViewModel() {
    private val _openScreen = MutableStateFlow<String?>(null)
    val openScreen = _openScreen.asStateFlow()

    suspend fun restoreAccounts() {
        val keyriProfiles = dataStore.data.first()
        val actualAccounts = keyri.listUniqueAccounts().getOrThrow()

        dataStore.updateData {
            val mappedProfiles =
                keyriProfiles.profiles.map {
                    if (it.associationKey != actualAccounts[it.email]) {
                        it.copy(associationKey = null)
                    } else {
                        it
                    }
                }

            keyriProfiles.copy(profiles = mappedProfiles)
        }
    }

    suspend fun checkPhoneVerifyState() {
        val profiles = dataStore.data.first()

        profiles.profiles
            .firstOrNull { it.email == profiles.currentProfile }
            ?.let {
                if (it.isVerify && it.verifyState?.isVerificationDone() == true && !it.biometricsSet && it.phone != null) {
                    _openScreen.value = Routes.VerifiedScreen.name
                }
            }
    }

    suspend fun getInitialScreen(data: Uri?) {
        if (data != null) {
            _openScreen.value = getScreenByLink(data)

            return
        }

        val profiles = dataStore.data.first()
        var screenToOpen = Routes.WelcomeScreen.name

        profiles.profiles
            .firstOrNull { it.email == profiles.currentProfile }
            ?.let { profile ->
                screenToOpen =
                    if (profile.verifyState?.isVerificationDone() == true && !profile.biometricsSet) {
                        Routes.VerifiedScreen.name
                    } else if (profile.verifyState?.isVerificationDone() != true && profile.verifyState is VerifyingState.EmailPhone) {
                        "${Routes.VerifyScreen.name}?name=${profile.name}&email=${profile.email}&number=${profile.phone}&isVerify=${profile.isVerify}"
                    } else {
                        Routes.WelcomeScreen.name
                    }
            }

        _openScreen.value = screenToOpen
    }

    suspend fun getScreenByLink(data: Uri): String {
        var screenToOpen = Routes.WelcomeScreen.name

        if (data.toString().contains("keyri-firebase-passkeys.vercel.app")) {
            data.getQueryParameter("sessionId")?.let {
                return "${Routes.WelcomeScreen.name}?sessionId=$it"
            }
        }

        val customToken =
            data
                .toString()
                .replaceBefore(CUSTOM_TOKEN_PARAM, "")
                .replace(CUSTOM_TOKEN_PARAM, "")

        dataStore.updateData { profiles ->
            val mappedProfiles =
                profiles.profiles.map {
                    if (it.email == profiles.currentProfile) {
                        if (it.verifyState?.isVerificationDone() == true && it.biometricsSet) {
                            screenToOpen = Routes.WelcomeScreen.name

                            it
                        } else if (it.verifyState?.isVerificationDone() == true) {
                            screenToOpen = Routes.VerifiedScreen.name

                            it
                        } else {
                            val newVerifyState =
                                when (it.verifyState) {
                                    is VerifyingState.Email -> {
                                        screenToOpen = Routes.VerifiedScreen.name

                                        it.verifyState.copy(isVerified = true)
                                    }

                                    is VerifyingState.EmailPhone -> {
                                        screenToOpen = Routes.VerifiedScreen.name

                                        it.verifyState.copy(emailVerified = true)
                                    }

                                    else -> {
                                        screenToOpen = Routes.WelcomeScreen.name

                                        it.verifyState
                                    }
                                }

                            it.copy(verifyState = newVerifyState, customToken = customToken)
                        }
                    } else {
                        it
                    }
                }

            profiles.copy(profiles = mappedProfiles)
        }

        return screenToOpen
    }
}
