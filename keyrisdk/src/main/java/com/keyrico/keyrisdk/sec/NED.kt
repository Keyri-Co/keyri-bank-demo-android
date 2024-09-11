@file:Suppress("Deprecation")

package com.keyrico.keyrisdk.sec

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_KEY
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_PLATFORM
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.reflect.Method
import java.util.Locale

internal class NED(
    private val blockSwizzleDetection: Boolean,
) {
    init {
        checkFakeNonKeyriInstance(blockSwizzleDetection)
    }

    private val listPackageName: MutableList<String> =
        mutableListOf(
            "com.google.android.launcher.layouts.genymotion",
            "com.bluestacks",
            "com.bignox.app",
        )

    fun checkNED(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkNED") {
            if (checkBasic()) {
                return@returnWithLog true
            }

            if (checkAdvanced(context)) {
                return@returnWithLog true
            }

            if (checkPackageNames(context)) {
                return@returnWithLog true
            }

            false
        }
    }

    private fun checkBasic(): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkBasic") {
            val defaultLocale = Locale.getDefault()

            var result =
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                    Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.lowercase(defaultLocale).contains("droid4x") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.HARDWARE == "goldfish" ||
                    Build.HARDWARE.contains("ranchu") ||
                    Build.HARDWARE == "vbox86" ||
                    Build.PRODUCT == "sdk" ||
                    Build.PRODUCT == "google_sdk" ||
                    Build.PRODUCT == "sdk_x86" ||
                    Build.PRODUCT == "vbox86p" ||
                    Build.BOARD.lowercase(defaultLocale).contains("nox") ||
                    Build.BOARD.lowercase(defaultLocale).contains("goldfish") ||
                    Build.BOOTLOADER.lowercase(defaultLocale).contains("nox") ||
                    Build.BOOTLOADER.lowercase(defaultLocale).contains("goldfish") ||
                    Build.HARDWARE.lowercase(defaultLocale).contains("nox") ||
                    Build.HARDWARE.lowercase(defaultLocale).contains("goldfish") ||
                    Build.PRODUCT.lowercase(defaultLocale).contains("nox") ||
                    Build.PRODUCT.lowercase(defaultLocale).contains("goldfish")

            if (result) return@returnWithLog true

            result =
                result or (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))

            if (result) return@returnWithLog true

            "google_sdk" == Build.PRODUCT
        }
    }

    private fun checkAdvanced(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkAdvanced") {
            checkTelephony(context) ||
                checkFiles(GENY_FILES) ||
                checkFiles(ANDY_FILES) ||
                checkFiles(NOX_FILES) ||
                checkQEmuDrivers() ||
                checkFiles(PIPES) ||
                checkIp(context) ||
                checkQEmuProps(context) &&
                checkFiles(X86_FILES)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun checkPackageNames(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkPackageName") {
            try {
                if (listPackageName.isEmpty()) {
                    return@returnWithLog false
                }

                val packageManager: PackageManager = context.packageManager

                for (pkgName in listPackageName) {
                    val tryIntent = packageManager.getLaunchIntentForPackage(pkgName)

                    if (tryIntent != null) {
                        val resolveInfos =
                            packageManager.queryIntentActivities(
                                tryIntent,
                                PackageManager.MATCH_DEFAULT_ONLY,
                            )

                        if (resolveInfos.isNotEmpty()) {
                            return@returnWithLog true
                        }
                    }
                }

                false
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun checkTelephony(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkTelephony") {
            var result = false

            try {
                result =
                    if ((
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_PHONE_STATE,
                            )
                                == PackageManager.PERMISSION_GRANTED
                        ) &&
                        isSupportTelePhony(context)
                    ) {
                        checkPhoneNumber(context) ||
                            checkDeviceId(context) ||
                            checkImsi(context) ||
                            checkOperatorNameAndroid(context)
                    } else {
                        false
                    }
            } catch (e: Exception) {
                Log.e(KEYRI_KEY, "failed to check telephony", e)
            }

            result
        }
    }

    private fun checkPhoneNumber(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkPhoneNumber") {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                @SuppressLint("HardwareIds", "MissingPermission")
                val phoneNumber = telephonyManager.line1Number

                for (number in EMULATOR_PHONE_NUMBERS) {
                    if (number.equals(phoneNumber, ignoreCase = true)) {
                        return@returnWithLog true
                    }
                }
            } catch (e: Exception) {
                Log.e(KEYRI_KEY, "failed to check phone number", e)
            }

            false
        }
    }

    private fun checkDeviceId(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkDeviceId") {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            try {
                @SuppressLint("HardwareIds", "MissingPermission")
                val deviceId = telephonyManager.deviceId

                for (knownDeviceId in DEVICE_IDS) {
                    if (knownDeviceId.equals(deviceId, ignoreCase = true)) {
                        return@returnWithLog true
                    }
                }
            } catch (e: Exception) {
                Log.e(KEYRI_KEY, "failed to check device id", e)
            }

            false
        }
    }

    private fun checkImsi(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkImsi") {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            try {
                @SuppressLint("HardwareIds", "MissingPermission")
                val imsi = telephonyManager.subscriberId

                for (knownImsi in IMSI_IDS) {
                    if (knownImsi.equals(imsi, ignoreCase = true)) {
                        return@returnWithLog true
                    }
                }
            } catch (e: Exception) {
                Log.e(KEYRI_KEY, "failed to check IMSI", e)
            }

            false
        }
    }

    private fun checkOperatorNameAndroid(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkOperatorNameAndroid") {
            val operatorName =
                (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkOperatorName
            if (operatorName.equals(KEYRI_PLATFORM, ignoreCase = true)) {
                return@returnWithLog true
            }

            false
        }
    }

    private fun checkQEmuDrivers(): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkQEmuDrivers") {
            for (driversFile in arrayOf(File("/proc/tty/drivers"), File("/proc/cpuinfo"))) {
                if (driversFile.exists() && driversFile.canRead()) {
                    val data = ByteArray(1024)

                    try {
                        val inputStream: InputStream = FileInputStream(driversFile)
                        inputStream.read(data)
                        inputStream.close()
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }

                    val driverData = String(data)

                    for (knownQemuDriver in QEMU_DRIVERS) {
                        if (driverData.contains(knownQemuDriver)) {
                            return@returnWithLog true
                        }
                    }
                }
            }

            false
        }
    }

    private fun checkFiles(targets: Array<String>): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkFiles") {
            for (pipe in targets) {
                val qemuFile = File(pipe)

                if (qemuFile.exists()) {
                    return@returnWithLog true
                }
            }

            false
        }
    }

    private fun checkQEmuProps(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkQEmuProps") {
            var foundProps = 0

            for (property in PROPERTIES) {
                val propertyValue = getProp(context, property.name)
                val seekValue = property.seekValue

                if (seekValue != null) {
                    if (propertyValue != null) {
                        foundProps++
                    }

                    if (propertyValue?.contains(seekValue) == true) {
                        foundProps++
                    }
                }
            }

            if (foundProps >= MIN_PROPERTIES_THRESHOLD) {
                return@returnWithLog true
            }

            false
        }
    }

    private fun checkIp(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkIp") {
            var ipDetected = false

            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    val args = arrayOf("/system/bin/netcfg")
                    val stringBuilder = StringBuilder()
                    try {
                        val builder = ProcessBuilder(*args)
                        builder.directory(File("/system/bin/"))
                        builder.redirectErrorStream(true)

                        val process = builder.start()
                        val inputStream: InputStream = process.inputStream
                        val re = ByteArray(1024)

                        while (inputStream.read(re) != -1) {
                            stringBuilder.append(String(re))
                        }

                        inputStream.close()
                    } catch (ex: Exception) {
                        // empty catch
                    }

                    val netData = stringBuilder.toString()

                    if (!TextUtils.isEmpty(netData)) {
                        val array = netData.split("\n".toRegex()).toTypedArray()

                        for (lan in array) {
                            if ((lan.contains("wlan0") || lan.contains("tunl0") || lan.contains("eth0")) &&
                                lan.contains(IP)
                            ) {
                                ipDetected = true
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(KEYRI_KEY, "failed to check emu", e)
            }

            ipDetected
        }
    }

    @SuppressLint("PrivateApi")
    private fun getProp(
        context: Context,
        property: String,
    ): String? {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        try {
            val systemProperties = context.classLoader.loadClass("android.os.SystemProperties")
            val get: Method = systemProperties.getMethod("get", String::class.java)
            val params = arrayOfNulls<Any>(1)

            params[0] = property

            return get.invoke(systemProperties, params)?.toString()
        } catch (exception: Exception) {
            // empty catch
        }

        return null
    }

    private fun isSupportTelePhony(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("isSupportTelePhony") {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        }
    }

    private fun returnWithLog(
        methodName: String,
        block: () -> Boolean,
    ): Boolean {
        val result = block()

        if (result) {
            Log.e(KEYRI_KEY, methodName)
        }

        return result
    }

    companion object {
        private val EMULATOR_PHONE_NUMBERS =
            arrayOf(
                "15555215554",
                "15555215556",
                "15555215558",
                "15555215560",
                "15555215562",
                "15555215564",
                "15555215566",
                "15555215568",
                "15555215570",
                "15555215572",
                "15555215574",
                "15555215576",
                "15555215578",
                "15555215580",
                "15555215582",
                "15555215584",
            )

        private val DEVICE_IDS = arrayOf("000000000000000", "e21833235b6eef10", "012345678912345")

        private val IMSI_IDS = arrayOf("310260000000000")

        private val GENY_FILES = arrayOf("/dev/socket/genyd", "/dev/socket/baseband_genyd")

        private val QEMU_DRIVERS = arrayOf("goldfish")

        private val PIPES = arrayOf("/dev/socket/qemud", "/dev/qemu_pipe")

        private val X86_FILES =
            arrayOf(
                "ueventd.android_x86.rc",
                "x86.prop",
                "ueventd.ttVM_x86.rc",
                "init.ttVM_x86.rc",
                "fstab.ttVM_x86",
                "fstab.vbox86",
                "init.vbox86.rc",
                "ueventd.vbox86.rc",
            )

        private val ANDY_FILES = arrayOf("fstab.andy", "ueventd.andy.rc")

        private val NOX_FILES = arrayOf("fstab.nox", "init.nox.rc", "ueventd.nox.rc")

        private val PROPERTIES: Array<Property> =
            arrayOf(
                Property("init.svc.qemud", null),
                Property("init.svc.qemu-props", null),
                Property("qemu.hw.mainkeys", null),
                Property("qemu.sf.fake_camera", null),
                Property("qemu.sf.lcd_density", null),
                Property("ro.bootloader", "unknown"),
                Property("ro.bootmode", "unknown"),
                Property("ro.hardware", "goldfish"),
                Property("ro.kernel.android.qemud", null),
                Property("ro.kernel.qemu.gles", null),
                Property("ro.kernel.qemu", "1"),
                Property("ro.product.device", "generic"),
                Property("ro.product.model", "sdk"),
                Property("ro.product.name", "sdk"),
                Property("ro.serialno", null),
            )

        private const val IP = "10.0.2.15"

        private const val MIN_PROPERTIES_THRESHOLD = 0x5

        class Property(
            var name: String,
            var seekValue: String?,
        )
    }
}
