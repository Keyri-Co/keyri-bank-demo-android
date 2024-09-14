package com.keyri.androidFullExample.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyriProfile(
    val name: String?,
    val email: String,
    val phone: String?,
    val isVerify: Boolean,
    val verifyState: VerifyingState?,
    val customToken: String?,
    val associationKey: String?,
    val biometricsSet: Boolean,
)

@Serializable
sealed class VerifyingState {
    fun isVerificationDone(): Boolean =
        when (this) {
            is Email -> isVerified
            is Phone -> isVerified
            is EmailPhone -> emailVerified && phoneVerified
        }

    @Serializable
    data class Email(
        val isVerified: Boolean = false,
    ) : VerifyingState()

    @Serializable
    data class Phone(
        val isVerified: Boolean = false,
    ) : VerifyingState()

    @Serializable
    data class EmailPhone(
        val emailVerified: Boolean = false,
        val phoneVerified: Boolean = false,
    ) : VerifyingState()
}
