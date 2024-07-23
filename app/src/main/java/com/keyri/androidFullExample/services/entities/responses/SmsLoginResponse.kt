package com.keyri.androidFullExample.services.entities.responses

import com.google.gson.annotations.SerializedName

data class SmsLoginResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("smsUrl")
    val smsUrl: SmsUrlResponse,
)

// TODO: Move to separate files

data class SmsUrlResponse(
    @SerializedName("confirmationId")
    val confirmationId: String,
    @SerializedName("confirmationMessage")
    val confirmationMessage: String,
    @SerializedName("confirmationUri")
    val confirmationUri: ConfirmationUriResponse,
    @SerializedName("sendTo")
    val sendTo: String,
    @SerializedName("expiresAt")
    val expiresAt: String,
)

data class ConfirmationUriResponse(
    @SerializedName("qr")
    val qr: String,
    @SerializedName("ios")
    val ios: String,
    @SerializedName("android")
    val android: String,
)
