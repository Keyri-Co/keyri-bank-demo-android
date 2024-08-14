package com.keyrico.keyrisdk.entity.associationkey

import com.google.gson.annotations.SerializedName

internal data class AccountResponse(
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String,
)
