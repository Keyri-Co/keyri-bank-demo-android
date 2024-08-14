package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class Template(
    @SerializedName("location")
    val location: String?,
    @SerializedName("issue")
    val issue: String?,
)
