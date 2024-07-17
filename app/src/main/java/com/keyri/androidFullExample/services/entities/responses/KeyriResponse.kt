package com.keyri.androidFullExample.services.entities.responses

import com.google.gson.annotations.SerializedName

data class KeyriResponse(
    @SerializedName("customToken")
    val customToken: String,
)
