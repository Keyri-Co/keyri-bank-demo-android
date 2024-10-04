@file:JvmName("EasyKeyriAuth")

package com.keyrico.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig

/**
 * Call it to delegate auth to Keyri SDK using preferable [androidx.activity.result.ActivityResult] API.
 *
 * @param context - context to open scanner screen.
 * @param easyKeyriAuthLauncher - declared [ActivityResultLauncher] which handle result.
 * @param appKey - appKey for given Origin.
 * @param publicApiKey - optional parameter for fingerprinting, null by default.
 * @param serviceEncryptionKey - optional parameter for fingerprinting, null by default.
 * @param payload - payload can be anything (session token or a stringified JSON containing multiple items.
 * Can include things like publicUserId, timestamp, data and ECDSA signature).
 * @param publicUserId - optional publicUserId, "ANON" or null for anonymous users.
 * @param detectionsConfig - configure security checks. See [KeyriDetectionsConfig].
 */
@JvmOverloads
fun easyKeyriAuth(
    context: Context,
    easyKeyriAuthLauncher: ActivityResultLauncher<Intent>,
    appKey: String,
    publicApiKey: String?,
    serviceEncryptionKey: String?,
    payload: String,
    publicUserId: String?,
    detectionsConfig: KeyriDetectionsConfig = KeyriDetectionsConfig(),
) {
    val intent =
        getAuthWithScannerIntent(
            context,
            appKey,
            publicApiKey,
            serviceEncryptionKey,
            payload,
            publicUserId,
            detectionsConfig,
        )

    easyKeyriAuthLauncher.launch(intent)
}

/**
 * Call it to delegate auth to Keyri SDK with using deprecated onActivityResult.
 *
 * @param activity - context to open scanner screen.
 * @param requestCode - request code to handle result.
 * @param appKey - appKey for given Origin.
 * @param publicApiKey - optional parameter for fingerprinting, null by default.
 * @param serviceEncryptionKey - optional parameter for fingerprinting, null by default.
 * @param payload - payload can be anything (session token or a stringified JSON containing multiple items.
 * Can include things like publicUserId, timestamp, data and ECDSA signature).
 * @param publicUserId - optional publicUserId, "ANON" or null for anonymous users.
 * @param detectionsConfig - configure security checks. See [KeyriDetectionsConfig].
 */
@Deprecated(
    message = "It uses deprecated onActivityResult. Use ActivityResult API instead.",
    replaceWith =
        ReplaceWith(
            expression =
                "easyKeyriAuth(" +
                    "activity," +
                    "easyKeyriAuthLauncher =," +
                    "appKey," +
                    "publicApiKey," +
                    "serviceEncryptionKey," +
                    "payload," +
                    "publicUserId," +
                    "detectionsConfig" +
                    ")",
        ),
)
@JvmOverloads
fun easyKeyriAuth(
    activity: Activity,
    requestCode: Int,
    appKey: String,
    publicApiKey: String? = null,
    serviceEncryptionKey: String? = null,
    payload: String,
    publicUserId: String?,
    detectionsConfig: KeyriDetectionsConfig = KeyriDetectionsConfig(),
) {
    val intent =
        getAuthWithScannerIntent(
            activity,
            appKey,
            publicApiKey,
            serviceEncryptionKey,
            payload,
            publicUserId,
            detectionsConfig,
        )

    activity.startActivityForResult(intent, requestCode)
}

private fun getAuthWithScannerIntent(
    context: Context,
    appKey: String,
    publicApiKey: String?,
    serviceEncryptionKey: String?,
    payload: String,
    publicUserId: String?,
    detectionsConfig: KeyriDetectionsConfig,
): Intent =
    Intent(context, ScannerAuthActivity::class.java).apply {
        putExtra(ScannerAuthActivity.APP_KEY, appKey)
        putExtra(ScannerAuthActivity.PUBLIC_API_KEY, publicApiKey)
        putExtra(ScannerAuthActivity.SERVICE_ENCRYPTION_KEY, serviceEncryptionKey)
        putExtra(
            ScannerAuthActivity.BLOCK_EMULATOR_DETECTION,
            detectionsConfig.blockEmulatorDetection,
        )
        putExtra(ScannerAuthActivity.BLOCK_ROOT_DETECTION, detectionsConfig.blockRootDetection)
        putExtra(
            ScannerAuthActivity.BLOCK_DANGEROUS_APPS_DETECTION,
            detectionsConfig.blockDangerousAppsDetection,
        )
        putExtra(ScannerAuthActivity.BLOCK_TAMPER_DETECTION, detectionsConfig.blockTamperDetection)
        putExtra(
            ScannerAuthActivity.BLOCK_SWIZZLE_DETECTION,
            detectionsConfig.blockSwizzleDetection,
        )
        putExtra(ScannerAuthActivity.PUBLIC_USER_ID, publicUserId)
        putExtra(ScannerAuthActivity.PAYLOAD, payload)
    }
