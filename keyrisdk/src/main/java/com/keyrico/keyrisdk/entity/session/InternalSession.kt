package com.keyrico.keyrisdk.entity.session

import com.google.gson.annotations.SerializedName

internal data class InternalSession(
    @SerializedName("WidgetOrigin")
    val widgetOrigin: String?,
    @SerializedName("sessionId")
    val sessionId: String?,
    @SerializedName("WidgetUserAgent")
    val widgetUserAgent: WidgetUserAgent?,
    @SerializedName("userParameters")
    val userParameters: UserParameters?,
    @SerializedName("IPAddressMobile")
    val iPAddressMobile: String?,
    @SerializedName("IPAddressWidget")
    val iPAddressWidget: String?,
    @SerializedName("riskAnalytics")
    val riskAnalytics: RiskAnalytics?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("browserPublicKey")
    val browserPublicKey: String?,
    @SerializedName("__salt")
    val salt: String?,
    @SerializedName("__hash")
    val hash: String?,
    @SerializedName("mobileTemplateResponse")
    val mobileTemplateResponse: MobileTemplateResponse?,
) {
    fun toSession(
        publicUserId: String,
        appKey: String,
        publicApiKey: String?,
        blockSwizzleDetection: Boolean,
    ): Session {
        return Session(
            widgetOrigin = requireNotNull(widgetOrigin),
            sessionId = requireNotNull(sessionId),
            widgetUserAgent = widgetUserAgent,
            userParameters = userParameters,
            iPAddressMobile = requireNotNull(iPAddressMobile),
            iPAddressWidget = requireNotNull(iPAddressWidget),
            riskAnalytics = riskAnalytics,
            publicUserId = publicUserId,
            mobileTemplateResponse = mobileTemplateResponse,
            message = message,
            appKey = appKey,
            browserPublicKey = requireNotNull(browserPublicKey),
            salt = requireNotNull(salt),
            hash = requireNotNull(hash),
            publicApiKey = publicApiKey,
            blockSwizzleDetection = blockSwizzleDetection,
        )
    }
}
