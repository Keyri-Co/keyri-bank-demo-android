package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class WidgetUserAgent(
    @SerializedName("electronVersion")
    val electronVersion: String,
    @SerializedName("isDesktop")
    val isDesktop: Boolean,
    @SerializedName("os")
    val os: String,
    @SerializedName("browser")
    val browser: String,
    @SerializedName("isAuthoritative")
    val isAuthoritative: Boolean,
    @SerializedName("isWindows")
    val isWindows: Boolean,
    @SerializedName("source")
    val source: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("isChrome")
    val isChrome: Boolean,
)
