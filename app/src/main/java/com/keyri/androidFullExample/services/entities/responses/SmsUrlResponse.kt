package com.keyri.androidFullExample.services.entities.responses

import com.google.gson.annotations.SerializedName

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
