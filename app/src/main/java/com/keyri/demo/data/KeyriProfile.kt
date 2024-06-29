package com.keyri.demo.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyriProfile(
    val name: String?,
    val email: String,
    val phone: String?,
    val biometricAuthEnabled: Boolean
)
