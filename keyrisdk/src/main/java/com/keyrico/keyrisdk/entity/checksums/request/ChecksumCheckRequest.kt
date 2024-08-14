package com.keyrico.keyrisdk.entity.checksums.request

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

data class ChecksumCheckRequest(
    @SerializedName("osType")
    val osType: String,
    @SerializedName("packageName")
    val packageName: String,
    @SerializedName("versionName")
    val versionName: String,
    @SerializedName("keyriVersion")
    val keyriVersion: String,
    @SerializedName("checksums")
    val checksums: JsonArray,
)
