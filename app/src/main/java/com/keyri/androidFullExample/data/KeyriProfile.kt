package com.keyri.androidFullExample.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyriProfile(
    val name: String?,
    val email: String,
    val phone: String?,
    val isVerify: Boolean,
    val isVerified: Boolean,
    val customToken: String?,
    val biometricAuthEnabled: Boolean,
)
