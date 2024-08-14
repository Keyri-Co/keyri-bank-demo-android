package com.keyrico.keyrisdk.entity

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("code")
    val code: Int?,
)
