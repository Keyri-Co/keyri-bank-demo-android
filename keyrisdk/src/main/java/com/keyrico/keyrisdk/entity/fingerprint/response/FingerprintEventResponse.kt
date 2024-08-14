package com.keyrico.keyrisdk.entity.fingerprint.response

import com.google.gson.annotations.SerializedName

data class FingerprintEventResponse(
    @SerializedName("apiCiphertextSignature")
    val apiCiphertextSignature: String,
    @SerializedName("publicEncryptionKey")
    val publicEncryptionKey: String,
    @SerializedName("ciphertext")
    val ciphertext: String,
    @SerializedName("iv")
    val iv: String,
    @SerializedName("salt")
    val salt: String,
)
