package com.keyrico.keyrisdk.entity.associationkey

import com.google.gson.annotations.SerializedName

internal data class ChangeAssociationKeysRequest(
    @SerializedName("payload")
    val payload: List<NewAssociationKey>,
    @SerializedName("deviceId")
    val deviceId: String?,
)
