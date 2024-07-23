package com.keyri.androidFullExample.screens.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.sec.fraud.event.EventType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class MainScreenViewModel(
    private val keyri: Keyri,
    private val dataStore: DataStore<KeyriProfiles>,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _loading = MutableStateFlow(true)
    private val _currentProfile = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val currentProfile = _currentProfile.asStateFlow()
    val loading = _loading.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()

    private val throwableScope =
        Dispatchers.IO +
            CoroutineExceptionHandler { _, throwable ->
                _errorMessage.value = throwable.message

                timer(initialDelay = 1_000L, period = 1_000L) {
                    _errorMessage.value = null
                }
            }

    init {
        getRiskDetails()
    }

    fun logout(callback: () -> Unit) {
        viewModelScope.launch(throwableScope) {
            dataStore.updateData { keyriProfiles ->
                keyriProfiles.copy(currentProfile = null).apply {
                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }
        }
    }

    private fun getRiskDetails() {
        // TODO: Remove logs
        Log.e("getting profile", "ok")

        viewModelScope.launch(throwableScope) {
            dataStore.data.collectLatest { keyriProfiles ->
                _currentProfile.value = keyriProfiles.currentProfile
            }

            // TODO: Remove logs
            Log.e("currentProfile", currentProfile.value.toString())

            currentProfile.value?.let {
                val eventResult = keyri.sendEvent(it, EventType.login(), true).getOrThrow()

                repository.decryptRisk(Gson().toJson(eventResult))

                _loading.value = false
            }

            _loading.value = false
        }
    }
}
