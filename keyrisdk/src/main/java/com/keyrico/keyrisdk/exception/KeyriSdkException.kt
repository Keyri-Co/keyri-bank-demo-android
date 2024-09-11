package com.keyrico.keyrisdk.exception

sealed class KeyriSdkException(
    override val message: String?,
) : Exception(message)

data class NoAssociationKeyPresentException(
    val publicUserId: String,
    override val message: String? = "No associationKey present for $publicUserId. Use generateAssociationKey() method first.",
) : KeyriSdkException(message)

data class NetworkException(
    override val message: String? = "No internet connection",
) : KeyriSdkException(message)

data class AuthorizationException(
    override val message: String? = "Unable to authorize",
) : KeyriSdkException(message)

data class RiskException(
    override val message: String? = "User Denied. Excessive Risk",
) : KeyriSdkException(message)

data class DenialException(
    override val message: String? = "Denied by user",
) : KeyriSdkException(message)

data class KeyriApiException(
    override val message: String? = "Keyri API error",
) : KeyriSdkException(message)

data class AccountDoesNotExistException(
    val publicUserId: String,
    override val message: String? = "$publicUserId does not exist on the device",
) : KeyriSdkException(message)

data class AccountAlreadyExistException(
    val publicUserId: String,
    override val message: String? = "$publicUserId already exists",
) : KeyriSdkException(message)
