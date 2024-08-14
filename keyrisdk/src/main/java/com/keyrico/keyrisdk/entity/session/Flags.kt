package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

data class Flags(
    @SerializedName("is_datacenter")
    val isDatacenter: Boolean?,
    @SerializedName("is_new_browser")
    val isNewBrowser: Boolean?,
)
