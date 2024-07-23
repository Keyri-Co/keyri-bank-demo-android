package com.keyri.androidFullExample.services.entities.responses

import com.google.gson.annotations.SerializedName

data class SmsLoginResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("smsUrl")
    val smsUrl: SmsUrlResponse,
)
