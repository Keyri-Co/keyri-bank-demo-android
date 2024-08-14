package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class UserAgent(
    @SerializedName("name")
    val name: String?,
    @SerializedName("issue")
    val issue: String?,
)
