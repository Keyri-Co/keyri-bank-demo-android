package com.keyri.androidFullExample.services.entities.requests

import com.google.gson.annotations.SerializedName

data class CryptoLoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("data")
    val data: String,
    @SerializedName("signatureB64")
    val signatureB64: String,
)
