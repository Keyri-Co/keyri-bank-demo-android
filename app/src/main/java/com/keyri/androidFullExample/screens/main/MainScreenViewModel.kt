package com.keyri.androidFullExample.screens.main

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyri.androidFullExample.services.entities.responses.LocationResponse
import com.keyri.androidFullExample.services.entities.responses.RiskResponse
import com.keyri.androidFullExample.utils.getIfHas
import com.keyri.androidFullExample.utils.getIfHasDouble
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.sec.fraud.event.EventType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.concurrent.timer

class MainScreenViewModel(
    private val keyri: Keyri,
    private val dataStore: DataStore<KeyriProfiles>,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _loading = MutableStateFlow(true)
    private val _currentProfile = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _riskResponse = MutableStateFlow<RiskResponse?>(null)
    val currentProfile = _currentProfile.asStateFlow()
    val loading = _loading.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()
    val riskResponse = _riskResponse.asStateFlow()

    private val throwableScope =
        CoroutineExceptionHandler { _, throwable ->
            _errorMessage.value = throwable.message

            throwable.let { e ->
                Firebase.crashlytics.recordException(e)
            }

            timer(initialDelay = 0L, period = 2_000L) {
                _errorMessage.value = null
            }
        }

    init {
        getRiskDetails()
    }

    fun logout(callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            dataStore.updateData { keyriProfiles ->
                keyriProfiles.copy(currentProfile = null).apply {
                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }
        }
    }

    fun generatePayload(
        email: String,
        result: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestampNonce =
                "${System.currentTimeMillis() / 1_000}${System.currentTimeMillis() / 1_000}"
            val signature = keyri.generateUserSignature(email, timestampNonce)

            val payloadJson = JSONObject()

            payloadJson.put("timestamp_nonce", timestampNonce)
            payloadJson.put("signature", signature)
            payloadJson.put("email", email)

            withContext(Dispatchers.Main) {
                result(payloadJson.toString())
            }
        }
    }

    private fun getRiskDetails() {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            dataStore.data.collectLatest { keyriProfiles ->
                _currentProfile.value = keyriProfiles.currentProfile

                keyriProfiles.currentProfile?.let {
                    val isVerify = keyriProfiles.profiles.first { p -> p.email == it }.isVerify

                    val eventType =
                        if (isVerify) {
                            EventType.signup()
                        } else {
                            EventType.login()
                        }

                    val eventResult = keyri.sendEvent(it, eventType, true).getOrThrow()

                    val gson = Gson()

                    val stringifiedResult =
                        repository.decryptRisk(gson.toJson(eventResult)).riskResponse

                    val obj = JSONObject(stringifiedResult)

                    val signals = obj.getJSONArray("signals")
                    val signalsMutable = mutableListOf<String>()

                    for (i in 0 until signals.length()) {
                        signalsMutable.add(signals[i].toString())
                    }

                    val locationObj = JSONObject(obj.getString("location"))

                    _riskResponse.value =
                        RiskResponse(
                            signals = signalsMutable,
                            location =
                                LocationResponse(
                                    city = locationObj.getIfHas("city"),
                                    regionCode = locationObj.getIfHas("region_code"),
                                    countryCode = locationObj.getIfHas("country_code"),
                                    continentCode = locationObj.getIfHas("continent_code"),
                                    latitude = locationObj.getIfHasDouble("latitude"),
                                    longitude = locationObj.getIfHasDouble("longitude"),
                                    ipTimezoneName = locationObj.getIfHas("ip_timezone_name"),
                                ),
                            fingerprintId = obj.getIfHas("fingerprintId"),
                            riskDetermination = obj.getIfHas("riskDetermination"),
                        )

                    _loading.value = false
                }
            }
        }
    }
}
