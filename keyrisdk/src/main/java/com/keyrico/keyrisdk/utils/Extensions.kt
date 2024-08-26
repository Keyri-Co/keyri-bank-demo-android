package com.keyrico.keyrisdk.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Base64
import com.keyrico.keyrisdk.sec.SNTPUtil
import com.keyrico.keyrisdk.sec.checkFakeInvocation
import java.security.MessageDigest

internal fun ByteArray.toStringBase64() = String(Base64.encode(this, Base64.NO_WRAP))

internal fun String.toByteArrayFromBase64String(): ByteArray =
    Base64.decode(toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

internal fun ByteArray.toSha1Base64() =
    MessageDigest.getInstance("SHA-1").digest(this).toStringBase64()

@SuppressLint("HardwareIds")
internal fun Context.getDeviceId(blockSwizzleDetection: Boolean): String? {
    checkFakeInvocation(blockSwizzleDetection)

    return try {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    } catch (e: Exception) {
        null
    }
}

internal fun getTimestampSeconds() = System.currentTimeMillis() / 1_000L

suspend fun getCorrectedTimestampSeconds(context: Context): Long {
    return if (Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME) == 0) {
        SNTPUtil.requestTimeMilliseconds().getOrNull()?.time?.div(1_000L) ?: getTimestampSeconds()
    } else {
        getTimestampSeconds()
    }
}
