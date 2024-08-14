package com.keyrico.keyrisdk.entity.login

import com.google.gson.annotations.SerializedName

/**
 * The `LoginObject` class represents login object.
 */
data class LoginObject(
    @SerializedName("timestampNonce")
    val timestampNonce: String,
    @SerializedName("signature")
    val signature: String,
    @SerializedName("publicKey")
    val publicKey: String,
    @SerializedName("userId")
    val userId: String,
)
