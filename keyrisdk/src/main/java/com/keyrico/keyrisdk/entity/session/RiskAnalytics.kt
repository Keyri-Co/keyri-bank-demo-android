package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class RiskAnalytics(
    @SerializedName("riskStatus")
    val riskStatus: String?,
    @SerializedName("riskFlagString")
    val riskFlagString: String?,
    @SerializedName("geoData")
    val geoData: GeoData?,
) {
    fun isDeny(): Boolean = riskStatus == "deny"
}
