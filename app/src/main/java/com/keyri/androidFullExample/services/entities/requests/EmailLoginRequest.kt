package com.keyri.androidFullExample.services.entities.requests

import com.google.gson.annotations.SerializedName

data class EmailLoginRequest(
    @SerializedName("email")
    val email: String,
)
