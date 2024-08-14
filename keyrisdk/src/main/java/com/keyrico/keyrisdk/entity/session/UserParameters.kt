package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class UserParameters(
    @SerializedName("base64EncodedData")
    val base64EncodedData: String?,
)
