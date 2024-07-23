package com.keyri.androidFullExample.services.entities.responses

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("city")
    val city: String?,
    @SerializedName("region_code")
    val regionCode: String?,
    @SerializedName("country_code")
    val countryCode: String?,
    @SerializedName("continent_code")
    val continentCode: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("ip_timezone_name")
    val ipTimezoneName: String?,
)
