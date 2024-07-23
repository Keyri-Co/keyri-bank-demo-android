package com.keyri.androidFullExample.services.entities.requests

import com.google.gson.annotations.SerializedName

data class ReverseSmsLoginRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("fcmToken")
    val fcmToken: String,
)
