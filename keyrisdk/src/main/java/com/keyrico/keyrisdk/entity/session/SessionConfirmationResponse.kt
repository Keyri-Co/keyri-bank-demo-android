package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class SessionConfirmationResponse(
    @SerializedName("status")
    val status: String?,
)
