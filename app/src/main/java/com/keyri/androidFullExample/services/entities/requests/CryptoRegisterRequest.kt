package com.keyri.androidFullExample.services.entities.requests

import com.google.gson.annotations.SerializedName

data class CryptoRegisterRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("associationKey")
    val associationKey: String,
    @SerializedName("idToken")
    val idToken: String,
)
