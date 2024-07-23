package com.keyri.androidFullExample.screens.verify

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyri.androidFullExample.services.entities.responses.SmsLoginResponse
import com.keyrico.keyrisdk.Keyri
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class VerifyViewModel(
    private val keyri: Keyri,
    private val dataStore: DataStore<KeyriProfiles>,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val throwableScope = Dispatchers.IO +
            CoroutineExceptionHandler { _, throwable ->
                _errorMessage.value = throwable.message

                timer(initialDelay = 1_000L, period = 1_000L) {
                    _errorMessage.value = null
                }
            }

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

    fun userRegister(
        isVerify: Boolean,
        name: String,
        email: String,
        phone: String?,
        onSuccess: (SmsLoginResponse) -> Unit
    ) {
        viewModelScope.launch(throwableScope) {
            val result = repository.userRegister(isVerify, name, email, phone)

            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        }
    }

    fun emailLogin(
        isVerify: Boolean,
        email: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(throwableScope) {
            repository.emailLogin(isVerify, email)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun smsLogin(
        isVerify: Boolean,
        email: String,
        number: String,
        onSuccess: (SmsLoginResponse) -> Unit
    ) {
        viewModelScope.launch(throwableScope) {
            val result = repository.smsLogin(isVerify, email, number)

            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        }
    }
}
