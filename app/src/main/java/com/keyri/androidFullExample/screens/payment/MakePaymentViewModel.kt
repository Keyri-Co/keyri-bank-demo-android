package com.keyri.androidFullExample.screens.payment

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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.concurrent.timer

class MakePaymentViewModel(
    private val keyri: Keyri,
    private val dataStore: DataStore<KeyriProfiles>,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _riskResult = MutableStateFlow<String?>(null)
    val loading = _loading.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()
    val riskResult = _riskResult.asStateFlow()

    private val throwableScope =
        CoroutineExceptionHandler { _, throwable ->
            _errorMessage.value = throwable.message

            timer(initialDelay = 1_000L, period = 1_000L) {
                _errorMessage.value = null
            }
        }

    fun performMakePaymentEvent(
        recipient: String,
        amount: Float,
        result: () -> Unit,
    ) {
        _loading.value = true

        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            dataStore.data
                .mapNotNull { it.currentProfile }
                .collectLatest { email ->
                    val eventResult =
                        keyri
                            .sendEvent(
                                email,
                                EventType.withdrawal(
                                    metadata =
                                        JSONObject().apply {
                                            put("recipient", recipient)
                                            put("amount", amount)
                                        },
                                ),
                                true,
                            ).getOrThrow()

                    val stringifiedResult =
                        repository.decryptRisk(Gson().toJson(eventResult)).riskResponse

                    withContext(Dispatchers.Main) {
                        _riskResult.value = stringifiedResult
                        result()
                    }

                    _loading.value = false
                }
        }
    }
}
