package com.keyrico.keyrisdk.entity.register

import com.google.gson.annotations.SerializedName

/**
 * The `RegisterObject` class represents register object.
 */
data class RegisterObject(
    @SerializedName("publicKey")
    val publicKey: String,
    @SerializedName("userId")
    val userId: String,
)
