package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class GeoData(
    @SerializedName("mobile")
    val mobile: IPData?,
    @SerializedName("browser")
    val browser: IPData?,
)
