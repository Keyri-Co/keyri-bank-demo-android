package com.keyrico.keyrisdk.entity.session

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.keyrico.keyrisdk.exception.AuthorizationException
import com.keyrico.keyrisdk.exception.RiskException
import com.keyrico.keyrisdk.services.CryptoService
import com.keyrico.keyrisdk.services.api.data.ApiData
import com.keyrico.keyrisdk.services.api.data.BrowserData
import com.keyrico.keyrisdk.services.api.data.SessionConfirmationRequest
import com.keyrico.keyrisdk.telemetry.TelemetryCodes
import com.keyrico.keyrisdk.telemetry.TelemetryManager
import com.keyrico.keyrisdk.utils.getDeviceId
import com.keyrico.keyrisdk.utils.makeApiCall
import com.keyrico.keyrisdk.utils.provideApiService
import org.json.JSONObject

/**
 * The `Session` class represents authentication Session for given sessionId.
 */
data class Session(
    @SerializedName("widgetOrigin")
    val widgetOrigin: String,
    @SerializedName("sessionId")
    val sessionId: String,
    @SerializedName("widgetUserAgent")
    val widgetUserAgent: WidgetUserAgent?,
    @SerializedName("userParameters")
    val userParameters: UserParameters?,
    @SerializedName("iPAddressMobile")
    val iPAddressMobile: String,
    @SerializedName("iPAddressWidget")
    val iPAddressWidget: String,
    @SerializedName("riskAnalytics")
    val riskAnalytics: RiskAnalytics?,
    @SerializedName("publicUserId")
    var publicUserId: String,
    @SerializedName("mobileTemplateResponse")
    val mobileTemplateResponse: MobileTemplateResponse?,
    private val message: String?,
    private val appKey: String,
    private val browserPublicKey: String,
    private val salt: String,
    private val hash: String,
    private val publicApiKey: String?,
    private val blockSwizzleDetection: Boolean,
) {
    /**
     * Call this function if user confirmed the dialog. Returns Boolean authentication result.
     *
     * @param payload - payload can be anything (session token or a stringified JSON containing multiple items.
     * Can include things like publicUserId, timestamp, data and ECDSA signature).
     * @return [Result] of session confirmation or error.
     */
    suspend fun confirm(
        payload: String,
        context: Context,
        trustNewBrowser: Boolean = false,
    ): Result<Unit> = finishSession(payload, true, context, trustNewBrowser)

    /**
     * Call if the user denied the dialog. Returns Boolean denial result.
     *
     * @param payload - payload can be anything (session token or a stringified JSON containing multiple items.
     * Can include things like publicUserId, timestamp, data and ECDSA signature).
     * @return [Result] for session denial or error.
     */
    suspend fun deny(
        payload: String,
        context: Context,
    ): Result<Unit> = finishSession(payload, false, context)

    private suspend fun finishSession(
        payload: String,
        isConfirmed: Boolean,
        context: Context,
        trustNewBrowser: Boolean = false,
    ): Result<Unit> =
        try {
            if (riskAnalytics?.isDeny() == true) {
                throw RiskException(message)
            }

            val custom =
                JSONObject()
                    .apply {
                        put("payload", payload)
                        put("deviceId", context.getDeviceId(blockSwizzleDetection))
                    }.toString()

            val cryptoService = CryptoService(context, appKey, blockSwizzleDetection)
            val cipher = cryptoService.encryptHkdf(browserPublicKey, custom)

            TelemetryManager.sendEvent(context, TelemetryCodes.PAYLOAD_ENCRYPTED)

            val associationKey = cryptoService.getAssociationKey(publicUserId)
            val apiData = ApiData(publicUserId, associationKey, associationKey)

            val browserData =
                BrowserData(
                    publicKey = cipher.publicKey,
                    ciphertext = cipher.cipherText,
                    salt = cipher.salt,
                    iv = cipher.iv,
                )

            val request =
                SessionConfirmationRequest(
                    salt = salt,
                    hash = hash,
                    errors = !isConfirmed,
                    dontTrustBrowser = !trustNewBrowser,
                    errorMsg = "",
                    apiData = apiData,
                    browserData = browserData,
                )

            TelemetryManager.sendEvent(context, TelemetryCodes.POST_SENT)

            val result =
                makeApiCall(context, blockSwizzleDetection) {
                    provideApiService(blockSwizzleDetection).approveSession(sessionId, request)
                }

            TelemetryManager.sendEvent(context, TelemetryCodes.POST_RESPONSE_RECEIVED)

            if (result.isSuccess) {
                TelemetryManager.sendEvent(context, TelemetryCodes.KEY_EXCHANGE_SUCCEEDED)

                Result.success(Unit)
            } else {
                val error = result.exceptionOrNull() ?: AuthorizationException()

                TelemetryManager.sendEvent(context, error)
                Result.failure(error)
            }
        } catch (error: Exception) {
            TelemetryManager.sendEvent(context, error)
            Result.failure(error)
        }.apply {
            TelemetryManager.lastSessionId = null
        }
}
