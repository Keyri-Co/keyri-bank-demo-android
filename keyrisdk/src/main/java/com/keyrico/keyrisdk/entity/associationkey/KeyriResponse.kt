package com.keyrico.keyrisdk.entity.associationkey

import com.google.gson.annotations.SerializedName
import com.keyrico.keyrisdk.entity.ErrorResponse

internal data class KeyriResponse<T>(
    @SerializedName("result")
    val result: Boolean,
    @SerializedName("error")
    val error: ErrorResponse?,
    @SerializedName("data")
    val data: T?,
)
