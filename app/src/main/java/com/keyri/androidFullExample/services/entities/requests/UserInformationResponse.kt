package com.keyri.androidFullExample.services.entities.requests

import com.google.gson.annotations.SerializedName

data class UserInformationResponse(
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
)
