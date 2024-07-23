package com.keyri.androidFullExample.services.entities.requests

import com.google.gson.annotations.SerializedName

data class DecryptRiskRequest(
    @SerializedName("encryptedEventString")
    val encryptedEventString: String,
)
