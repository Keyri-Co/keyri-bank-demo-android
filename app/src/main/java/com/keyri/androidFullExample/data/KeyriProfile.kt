package com.keyri.androidFullExample.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyriProfile(
    val name: String?,
    val email: String,
    val phone: String?,
    val isVerify: Boolean,
    val emailVerifyState: VerifyingState,
    val phoneVerifyState: VerifyingState,
    val customToken: String?,
)

@Serializable
enum class VerifyingState {
    NOT_VERIFIED,
    VERIFYING,
    VERIFIED,
}
