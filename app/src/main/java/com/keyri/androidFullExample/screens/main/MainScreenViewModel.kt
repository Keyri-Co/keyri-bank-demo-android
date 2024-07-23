package com.keyri.androidFullExample.screens.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.keyri.androidFullExample.data.KeyriProfiles
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import com.keyri.androidFullExample.services.entities.responses.LocationResponse
import com.keyri.androidFullExample.services.entities.responses.RiskResponse
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
import java.io.StringReader
import kotlin.concurrent.timer
import kotlin.math.sign


class MainScreenViewModel(
    private val keyri: Keyri,
    private val dataStore: DataStore<KeyriProfiles>,
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _loading = MutableStateFlow(true)
    private val _currentProfile = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _decryptRisk = MutableStateFlow<LocationResponse?>(null)
    private val _riskResponse = MutableStateFlow<RiskResponse?>(null)
    val currentProfile = _currentProfile.asStateFlow()
    val loading = _loading.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()
    val decryptRisk = _decryptRisk.asStateFlow()
    val riskResponse = _riskResponse.asStateFlow()

    private val throwableScope =
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

    private fun getRiskDetails() {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            dataStore.data.collectLatest { keyriProfiles ->
                _currentProfile.value = keyriProfiles.currentProfile

                keyriProfiles.currentProfile?.let {
                    val eventResult = keyri.sendEvent(it, EventType.login(), true).getOrThrow()

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

                    _riskResponse.value = RiskResponse(
                        signals = signalsMutable,
                        location = LocationResponse(
                            city = locationObj.getIfHas("city"),
                            regionCode = locationObj.getIfHas("region_code"),
                            countryCode = locationObj.getIfHas("country_code"),
                            continentCode = locationObj.getIfHas("continent_code"),
                            latitude = locationObj.getIfHasDouble("latitude"),
                            longitude = locationObj.getIfHasDouble("longitude"),
                            ipTimezoneName = locationObj.getIfHas("ip_timezone_name"),
                        ),
                        fingerprintId = obj.getIfHas("fingerprintId"),
                        riskDetermination = obj.getIfHas("riskDetermination")
                    )

                    _loading.value = false
                }
            }
        }
    }

    private fun JSONObject.getIfHas(fieldName: String): String? {
        return takeIf { it.has(fieldName) }?.getString(fieldName)
    }

    private fun JSONObject.getIfHasDouble(fieldName: String): Double? {
        return takeIf { it.has(fieldName) }?.getDouble(fieldName)
    }
}
