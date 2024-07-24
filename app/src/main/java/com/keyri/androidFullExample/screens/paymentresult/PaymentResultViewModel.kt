package com.keyri.androidFullExample.screens.paymentresult

import androidx.lifecycle.ViewModel
import com.keyri.androidFullExample.services.entities.responses.LocationResponse
import com.keyri.androidFullExample.services.entities.responses.RiskResponse
import com.keyri.androidFullExample.utils.getIfHas
import com.keyri.androidFullExample.utils.getIfHasDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class PaymentResultViewModel : ViewModel() {
    private val _riskResponse = MutableStateFlow<RiskResponse?>(null)
    val riskResponse = _riskResponse.asStateFlow()

    fun processRiskResult(riskResult: String) {
        val obj = JSONObject(riskResult)

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
    }
}
