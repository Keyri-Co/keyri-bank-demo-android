package com.keyrico.keyrisdk.entity.fingerprint.request

import com.google.gson.annotations.SerializedName

internal data class DeviceInfoRequest(
    @SerializedName("platform")
    val platform: String?,
    @SerializedName("deviceType")
    val deviceType: String?,
    @SerializedName("uniqueDeviceHash")
    val uniqueDeviceHash: String?,
    @SerializedName("deviceName")
    val deviceName: String?,
    @SerializedName("installationSource")
    val installationSource: String?,
    @SerializedName("appId")
    val appId: String?,
    @SerializedName("appSignature")
    val appSignature: List<String>?,
    @SerializedName("cpuInformation")
    val cpuInformation: String?,
    @SerializedName("kernelVersion")
    val kernelVersion: String?,
    @SerializedName("supportedCodecs")
    val supportedCodecs: List<String>,
    @SerializedName("availableLocales")
    val availableLocales: List<String>,
    @SerializedName("systemApps")
    val systemApps: List<String>,
    @SerializedName("sensors")
    val sensors: List<String>,
    @SerializedName("cameras")
    val cameras: List<String>,
    @SerializedName("supportedABIs")
    val supportedABIs: List<String>,
    @SerializedName("screenResolution")
    val screenResolution: String?,
    @SerializedName("availableResolution")
    val availableResolution: String?,
    @SerializedName("totalRAMMemory")
    val totalRAMMemory: String?,
    @SerializedName("totalStorageMemory")
    val totalStorageMemory: String?,
    @SerializedName("carrierInformation")
    val carrierInformation: String?,
    @SerializedName("isHighTextContrastEnabled")
    val isHighTextContrastEnabled: Boolean?,
    @SerializedName("screenColorDepth")
    val screenColorDepth: Int?,
    @SerializedName("hardwareConcurrency")
    val hardwareConcurrency: Int?,
    @SerializedName("maxTouchPoints")
    val maxTouchPoints: Int?,
    @SerializedName("isHdr")
    val isHdr: Boolean?,
    @SerializedName("isInversionModeEnabled")
    val isInversionModeEnabled: Boolean?,
    @SerializedName("mathFingerprint")
    val mathFingerprint: String?,
)
