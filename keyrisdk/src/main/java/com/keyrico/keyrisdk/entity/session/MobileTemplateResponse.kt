package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class MobileTemplateResponse(
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String?,
    @SerializedName("widget")
    val widget: Template?,
    @SerializedName("mobile")
    val mobile: Template?,
    @SerializedName("userAgent")
    val userAgent: UserAgent?,
    @SerializedName("flags")
    val flags: Flags?,
)
