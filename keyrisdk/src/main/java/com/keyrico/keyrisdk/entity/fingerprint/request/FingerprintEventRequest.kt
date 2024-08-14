package com.keyrico.keyrisdk.entity.fingerprint.request

import com.google.gson.annotations.SerializedName

data class FingerprintEventRequest(
    @SerializedName("clientEncryptionKey")
    val clientEncryptionKey: String,
    @SerializedName("encryptedPayload")
    val encryptedPayload: String,
    @SerializedName("iv")
    val iv: String,
    @SerializedName("salt")
    val salt: String,
)
