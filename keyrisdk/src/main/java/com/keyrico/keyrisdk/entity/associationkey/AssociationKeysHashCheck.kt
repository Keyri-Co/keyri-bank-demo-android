package com.keyrico.keyrisdk.entity.associationkey

import com.google.gson.annotations.SerializedName

internal data class AssociationKeysHashCheck(
    @SerializedName("hash")
    val hash: String,
    @SerializedName("identic")
    val identical: Boolean,
    @SerializedName("accountsCount")
    val accountsCount: Int,
)
