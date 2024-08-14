package com.keyrico.keyrisdk.sec.fraud

import android.content.Context
import android.util.Log
import com.keyrico.keyrisdk.Keyri.Companion.ANON_USER
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_KEY
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig
import com.keyrico.keyrisdk.entity.fingerprint.request.FingerprintEventRequest
import com.keyrico.keyrisdk.entity.fingerprint.response.FingerprintEventResponse
import com.keyrico.keyrisdk.sec.fraud.event.EventType
import com.keyrico.keyrisdk.services.CryptoService
import com.keyrico.keyrisdk.utils.getCorrectedTimestampSeconds
import com.keyrico.keyrisdk.utils.makeApiCall
import com.keyrico.keyrisdk.utils.provideFraudApiService
import org.json.JSONObject

internal class FraudService(private val detectionsConfig: KeyriDetectionsConfig) {
    suspend fun getFingerprintEventPayload(
        context: Context,
        cryptoService: CryptoService,
        publicApiKey: String?,
        publicUserId: String? = null,
        serviceEncryptionKey: String?,
        eventType: EventType? = null,
        success: Boolean? = null,
    ): Result<FingerprintEventRequest> {
        if (publicApiKey == null) {
            val exceptionMessage =
                "$FAILED_TO_SEND_EVENT_MESSAGE: you need to provide valid publicApiKey"

            Log.e(KEYRI_KEY, exceptionMessage)

            return Result.failure(Exception(exceptionMessage))
        }

        if (serviceEncryptionKey == null) {
            val exceptionMessage =
                "$FAILED_TO_SEND_EVENT_MESSAGE: you need to provide valid serviceEncryptionKey"

            Log.e(KEYRI_KEY, exceptionMessage)

            return Result.failure(Exception(exceptionMessage))
        }

        val associationKey =
            cryptoService.getAssociationKey(ANON_USER) ?: cryptoService.generateAssociationKey(
                ANON_USER,
            )

        val timestamp = getCorrectedTimestampSeconds(context).toString()
        val timestampSignature = cryptoService.signMessage(ANON_USER, timestamp)

        val deviceInfo = DeviceInfo()
        val deviceInfoHash = deviceInfo.getDeviceInfoHash(context)
        val deviceInfoJson = deviceInfo.getSignalsObject(context, false)

        val plainTextPayload =
            JSONObject().apply {
                put("apiKey", publicApiKey)
                eventType?.name?.let { put("eventType", it) } ?: put(
                    "eventType",
                    EventType.visits().name,
                )
                eventType?.metadata?.let { put("eventMetadata", it) }
                success?.let { put("success", it) }
                publicUserId?.let { put("userId", it) }
                put("timestamp", timestamp)
                put("timestampSignature", timestampSignature)
                put("deviceHash", deviceInfoHash)
                put("deviceInfo", deviceInfoJson)
                put("cryptoCookie", associationKey)
                put("serviceEncryptionKey", serviceEncryptionKey)
            }.toString()

        val encryptionResult = cryptoService.encryptHkdf(BACKEND_PUBLIC_KEY, plainTextPayload)

        return Result.success(
            FingerprintEventRequest(
                clientEncryptionKey = encryptionResult.publicKey,
                encryptedPayload = encryptionResult.cipherText,
                iv = encryptionResult.iv,
                salt = encryptionResult.salt,
            ),
        )
    }

    suspend fun sendEvent(
        context: Context,
        cryptoService: CryptoService,
        publicApiKey: String?,
        publicUserId: String,
        serviceEncryptionKey: String?,
        eventType: EventType,
        success: Boolean,
    ): Result<FingerprintEventResponse> {
        val payloadResult =
            getFingerprintEventPayload(
                context,
                cryptoService,
                publicApiKey,
                publicUserId,
                serviceEncryptionKey,
                eventType,
                success,
            ).getOrThrow()

        val blockSwizzleDetection = detectionsConfig.blockSwizzleDetection

        val fingerprintEventResult =
            makeApiCall(context, blockSwizzleDetection) {
                provideFraudApiService(blockSwizzleDetection).sendFingerprintEvent(payloadResult)
            }

        return if (fingerprintEventResult.isSuccess) {
            fingerprintEventResult
        } else {
            val exception = fingerprintEventResult.exceptionOrNull()

            Log.e(KEYRI_KEY, FAILED_TO_SEND_EVENT_MESSAGE, exception)

            Result.failure(exception ?: Exception(FAILED_TO_SEND_EVENT_MESSAGE))
        }
    }

    companion object {
        private const val FAILED_TO_SEND_EVENT_MESSAGE = "Failed to send fingerprint event"
        private const val BACKEND_PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEO6aCXxDctj+urHBGTGQgfJ9I9euUIPtkLYMfloUqz1m/zUMIY26Ojz97C/o72DtcXh0xEi6gD/W/jIMvaUJEgw=="
    }
}
