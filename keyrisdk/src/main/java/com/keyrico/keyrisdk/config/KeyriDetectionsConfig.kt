package com.keyrico.keyrisdk.config

/**
 *
 * @param blockEmulatorDetection - set this param to false if you want to deny run your app on emulators, true by default.
 * @param blockRootDetection - set this param to true if you want to allow running your app without rooted device check, false by default.
 * @param blockDangerousAppsDetection - set this param to true if you want to allow running your app without dangerous apps check, false by default.
 * @param blockTamperDetection - set this param to true if you want to allow running your app without tamper detection check, true by default.
 * @param blockSwizzleDetection - set this param to true if you want to allow running your app without swizzle detection check, false by default.
 */
data class KeyriDetectionsConfig
    @JvmOverloads
    constructor(
        val blockEmulatorDetection: Boolean = true,
        val blockRootDetection: Boolean = false,
        val blockDangerousAppsDetection: Boolean = false,
        val blockTamperDetection: Boolean = true,
        val blockSwizzleDetection: Boolean = false,
    )
