package com.keyrico.keyrisdk.entity.associationkey

import com.google.gson.annotations.SerializedName

internal data class RemoveAssociationKeysRequest(
    @SerializedName("username")
    val username: String,
)
