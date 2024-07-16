package com.keyri.androidFullExample.services.entities.responses

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message")
    val message: String?,
)
