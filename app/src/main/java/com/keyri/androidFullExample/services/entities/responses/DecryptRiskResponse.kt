package com.keyri.androidFullExample.services.entities.responses

import com.google.gson.annotations.SerializedName

data class DecryptRiskResponse(
    @SerializedName("riskResponse")
    val riskResponse: String,
)

data class RiskResponse(
    @SerializedName("signals")
    val signals: List<String>,
    @SerializedName("location")
    val location: LocationResponse,
    @SerializedName("fingerprintId")
    val fingerprintId: String?,
    @SerializedName("riskDetermination")
    val riskDetermination: String?,
)
