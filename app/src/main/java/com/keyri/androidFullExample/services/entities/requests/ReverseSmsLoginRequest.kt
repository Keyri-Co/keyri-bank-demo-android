package com.keyri.androidFullExample.services.entities.requests

import com.google.gson.annotations.SerializedName

data class ReverseSmsLoginRequest(
    @SerializedName("number")
    val number: String,
)
