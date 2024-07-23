package com.keyri.androidFullExample.services.entities.responses

import com.google.gson.annotations.SerializedName

data class ConfirmationUriResponse(
    @SerializedName("qr")
    val qr: String,
    @SerializedName("ios")
    val ios: String,
    @SerializedName("android")
    val android: String,
)
