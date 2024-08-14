package com.keyrico.keyrisdk.entity.associationkey

import com.google.gson.annotations.SerializedName

internal data class NewAssociationKey(
    @SerializedName("username")
    val username: String,
    @SerializedName("oldKey")
    val oldKey: String?,
    @SerializedName("newKey")
    val newKey: String,
)
