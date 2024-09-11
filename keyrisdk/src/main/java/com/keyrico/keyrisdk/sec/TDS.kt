package com.keyrico.keyrisdk.sec

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.keyrico.keyrisdk.BuildConfig
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_PLATFORM
import com.keyrico.keyrisdk.entity.checksums.request.ChecksumCheckRequest
import com.keyrico.keyrisdk.utils.getApiCallResponseCode
import com.keyrico.keyrisdk.utils.provideChecksumApiService
import com.keyrico.keyrisdk.utils.toStringBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest
import java.util.jar.JarFile

internal class TDS(
    private val blockSwizzleDetection: Boolean,
) {
    init {
        checkFakeNonKeyriInstance(blockSwizzleDetection)
    }

    @Suppress("Deprecation")
    suspend fun checksumCheck(
        context: Context,
        appKey: String,
    ): Result<Unit> {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName

            val jarFile =
                withContext(Dispatchers.IO) {
                    val appInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            packageManager.getApplicationInfo(
                                packageName,
                                PackageManager.ApplicationInfoFlags.of(0.toLong()),
                            )
                        } else {
                            packageManager.getApplicationInfo(packageName, 0)
                        }

                    JarFile(appInfo.sourceDir)
                }

            val checksums = JsonArray()

            jarFile.entries().toList().forEach { entry ->
                val inputStream = jarFile.getInputStream(entry)
                val digest = digestAndStringBase64(inputStream)

                val bundleFileEntity = JsonObject()

                bundleFileEntity.addProperty(entry.name, digest)
                checksums.add(bundleFileEntity)
            }

            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0),
                    )
                } else {
                    packageManager.getPackageInfo(packageName, 0)
                }

            val version = packageInfo.versionName

            val request =
                ChecksumCheckRequest(
                    KEYRI_PLATFORM,
                    context.packageName,
                    version,
                    BuildConfig.VERSION,
                    checksums,
                )

            getApiCallResponseCode(blockSwizzleDetection) {
                provideChecksumApiService(
                    blockSwizzleDetection,
                ).sendChecksums(appKey, request)
            }.getOrNull()?.takeIf { code -> code == 409 }?.let {
                Result.failure(Exception("Failed to read file checksums"))
            } ?: Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    private fun digestAndStringBase64(inputStream: InputStream): String {
        val buffer = ByteArray(BUFFER_LENGTH)
        val digest = MessageDigest.getInstance("SHA-256")
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }

        return digest.digest().toStringBase64()
    }

    companion object {
        private const val BUFFER_LENGTH = 1024 * 4
    }
}
