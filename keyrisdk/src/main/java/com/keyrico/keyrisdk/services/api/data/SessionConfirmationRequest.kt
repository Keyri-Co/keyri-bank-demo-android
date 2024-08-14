package com.keyrico.keyrisdk.services.api.data

import com.google.gson.annotations.SerializedName

internal data class SessionConfirmationRequest(
    @SerializedName("__salt")
    val salt: String,
    @SerializedName("__hash")
    val hash: String,
    @SerializedName("errors")
    val errors: Boolean,
    @SerializedName("dontTrustBrowser")
    val dontTrustBrowser: Boolean,
    @SerializedName("errorMsg")
    val errorMsg: String,
    @SerializedName("apiData")
    val apiData: ApiData,
    @SerializedName("browserData")
    val browserData: BrowserData,
)
