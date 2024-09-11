@file:Suppress("Deprecation")

package com.keyrico.keyrisdk.sec.fraud

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo.COLOR_MODE_HDR
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.FEATURE_TOUCHSCREEN
import android.content.pm.PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH
import android.content.pm.PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.media.MediaCodecList
import android.os.Build
import android.os.Debug
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.gson.Gson
import com.keyrico.keyrisdk.BuildConfig
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_KEY
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_PLATFORM
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig
import com.keyrico.keyrisdk.entity.fingerprint.request.DeviceInfoRequest
import com.keyrico.keyrisdk.sec.NED
import com.keyrico.keyrisdk.sec.SIPD
import com.keyrico.keyrisdk.sec.checkFakeNonKeyriInvocation
import com.keyrico.keyrisdk.utils.getDeviceId
import com.keyrico.keyrisdk.utils.toSha1Base64
import com.keyrico.keyrisdk.utils.toStringBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.Locale
import java.util.Scanner
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atanh
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.expm1
import kotlin.math.ln1p
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.tan
import kotlin.math.tanh

internal class DeviceInfo {
    suspend fun getDeviceInfoJson(context: Context): String {
        val installationSource = checkInstallationSource(context)
        val carrierInfo = getCarrierInfo(context)
        val packageDetails = getPackageDetails(context)
        val resolutionInfo = getResolutionInfo(context)
        val totalRAMMemory = getTotalRAMMemory(context)
        val totalStorageMemory = getTotalStorageMemory()
        val cpuName = getCPUName()
        val kernelVersion = getKernelVersion()
        val supportedCodecs = getSupportedCodecs()
        val availableLocales = getAvailableLocales()
        val systemApps = getSystemApps(context)
        val sensorsList = getSensorsList(context)
        val camerasList = getCameraList(context)
        val supportedABIs = getSupportedABIs()
        val deviceName = getDeviceName(context)
        val userAgent = getUserAgent(context)
        val isHighTextContrastEnabled = isHighTextContrastEnabled(context)
        val screenColorDepth = getScreenColorDepth(context)
        val hardwareConcurrency = getHardwareConcurrency()
        val maxTouchPoints = getMaxTouchPoints(context)
        val isHdr = isHdr(context)
        val isInversionModeEnabled = isInversionModeEnabled(context)
        val mathFingerprint = getMathFingerprint()
        val resolution = "${resolutionInfo.widthPixels} x ${resolutionInfo.heightPixels}"
        val uniqueDeviceHash = context.getDeviceId(true)?.encodeToByteArray()?.toSha1Base64()

        val deviceInfo =
            DeviceInfoRequest(
                platform = KEYRI_PLATFORM,
                deviceType = "Mobile",
                uniqueDeviceHash = uniqueDeviceHash,
                deviceName = deviceName,
                installationSource = installationSource,
                appId = context.packageName,
                appSignature = packageDetails?.appSignatures ?: listOf(),
                cpuInformation = cpuName,
                kernelVersion = kernelVersion,
                supportedCodecs = supportedCodecs,
                availableLocales = availableLocales,
                systemApps = systemApps,
                sensors = sensorsList,
                cameras = camerasList,
                supportedABIs = supportedABIs,
                screenResolution = resolution,
                availableResolution = resolution,
                totalRAMMemory = totalRAMMemory,
                totalStorageMemory = totalStorageMemory,
                carrierInformation = carrierInfo,
                userAgent = userAgent,
                isHighTextContrastEnabled = isHighTextContrastEnabled,
                screenColorDepth = screenColorDepth,
                hardwareConcurrency = hardwareConcurrency,
                maxTouchPoints = maxTouchPoints,
                isHdr = isHdr,
                isInversionModeEnabled = isInversionModeEnabled,
                mathFingerprint = mathFingerprint,
            )

        return JSONObject(Gson().toJson(deviceInfo)).toString(4)
    }

    suspend fun getDeviceInfoHash(context: Context): String {
        val installationSource = checkInstallationSource(context)
        val carrierInfo = getCarrierInfo(context)
        val packageDetails = getPackageDetails(context)
        val resolutionInfo = getResolutionInfo(context)
        val totalRAMMemory = getTotalRAMMemory(context)
        val totalStorageMemory = getTotalStorageMemory()
        val cpuName = getCPUName()
        val kernelVersion = getKernelVersion()
        val supportedCodecs = getSupportedCodecs()
        val availableLocales = getAvailableLocales()
        val systemApps = getSystemApps(context)
        val sensorsList = getSensorsList(context)
        val camerasList = getCameraList(context)
        val supportedABIs = getSupportedABIs()
        val deviceName = getDeviceName(context)
        val userAgent = getUserAgent(context)
        val isHighTextContrastEnabled = isHighTextContrastEnabled(context)
        val screenColorDepth = getScreenColorDepth(context)
        val hardwareConcurrency = getHardwareConcurrency()
        val maxTouchPoints = getMaxTouchPoints(context)
        val isHdr = isHdr(context)
        val isInversionModeEnabled = isInversionModeEnabled(context)
        val mathFingerprint = getMathFingerprint()
        val resolution = "${resolutionInfo.widthPixels} x ${resolutionInfo.heightPixels}"
        val uniqueDeviceHash = context.getDeviceId(true)?.encodeToByteArray()?.toSha1Base64()

        val deviceInfo =
            DeviceInfoRequest(
                platform = KEYRI_PLATFORM,
                deviceType = "Mobile",
                uniqueDeviceHash = uniqueDeviceHash,
                deviceName = deviceName,
                installationSource = installationSource,
                appId = context.packageName,
                appSignature = packageDetails?.appSignatures ?: listOf(),
                cpuInformation = cpuName,
                kernelVersion = kernelVersion,
                supportedCodecs = supportedCodecs,
                availableLocales = availableLocales,
                systemApps = systemApps,
                sensors = sensorsList,
                cameras = camerasList,
                supportedABIs = supportedABIs,
                screenResolution = resolution,
                availableResolution = resolution,
                totalRAMMemory = totalRAMMemory,
                totalStorageMemory = totalStorageMemory,
                carrierInformation = carrierInfo,
                userAgent = userAgent,
                isHighTextContrastEnabled = isHighTextContrastEnabled,
                screenColorDepth = screenColorDepth,
                hardwareConcurrency = hardwareConcurrency,
                maxTouchPoints = maxTouchPoints,
                isHdr = isHdr,
                isInversionModeEnabled = isInversionModeEnabled,
                mathFingerprint = mathFingerprint,
            )

        val jsonPayload = Gson().toJson(deviceInfo).encodeToByteArray()

        return MessageDigest.getInstance("MD5").digest(jsonPayload).toStringBase64()
    }

    fun getSignalsObject(
        context: Context,
        tamperDetected: Boolean,
    ): JSONObject {
        val config =
            KeyriDetectionsConfig(
                blockEmulatorDetection = true,
                blockRootDetection = true,
                blockDangerousAppsDetection = true,
                blockTamperDetection = true,
                blockSwizzleDetection = true,
            )

        val swizzleDetection =
            try {
                checkFakeNonKeyriInvocation(config.blockSwizzleDetection)
                false
            } catch (e: Exception) {
                true
            }

        val dangerousApps = checkDangerousPackages(context)

        return JSONObject().apply {
            put("rooted", SIPD(config).checkSIPD(context))
            put("swizzle", swizzleDetection)
            put("isDebuggable", checkDebuggable(context))
            put("maliciousPackages", dangerousApps)
            put("dangerousApps", dangerousApps)
            put("tamperDetection", tamperDetected)
            put("emulator", NED(config.blockSwizzleDetection).checkNED(context))
        }
    }

    private suspend fun getUserAgent(context: Context): String {
        val sharedPreferences =
            context.getSharedPreferences(Keyri.BACKUP_KEYRI_PREFS, AppCompatActivity.MODE_PRIVATE)

        return sharedPreferences.getString(USER_AGENT_STRING_KEY, null)
            ?: withContext(Dispatchers.Main) {
                val userAgentString = WebView(context).settings.userAgentString

                sharedPreferences.edit(commit = true) {
                    putString(USER_AGENT_STRING_KEY, userAgentString)
                }

                userAgentString
            }
    }

    private fun isHighTextContrastEnabled(context: Context): Boolean =
        try {
            Settings.Secure.getInt(context.contentResolver, "high_text_contrast_enabled", 0) == 1
        } catch (e: Exception) {
            false
        }

    private fun getScreenColorDepth(context: Context): Int {
        val info = PixelFormat()
        val pixelFormat = (context as? Activity)?.windowManager?.defaultDisplay?.pixelFormat

        PixelFormat.getPixelFormatInfo(pixelFormat ?: PixelFormat.RGBA_8888, info)

        return info.bitsPerPixel
    }

    private fun getHardwareConcurrency(): Int = Runtime.getRuntime().availableProcessors() * 2

    private fun getMaxTouchPoints(context: Context): Int {
        var points = 0

        if (context.packageManager.hasSystemFeature(FEATURE_TOUCHSCREEN)) {
            points = 1
        }

        if (context.packageManager.hasSystemFeature(FEATURE_TOUCHSCREEN_MULTITOUCH)) {
            points = 2
        }

        if (context.packageManager.hasSystemFeature(FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND)) {
            points = 5
        }

        return points
    }

    private fun getSensorsList(context: Context): List<String> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?

        return sensorManager?.getSensorList(Sensor.TYPE_ALL)?.map {
            it.name + it.vendor
        } ?: listOf()
    }

    private fun getCameraList(context: Context): List<String> {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?

        return try {
            manager?.cameraIdList?.toList() ?: listOf()
        } catch (e: Exception) {
            listOf()
        }
    }

    private fun getSupportedABIs(): List<String> = Build.SUPPORTED_ABIS.toList()

    private fun isHdr(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context as? Activity)?.window?.colorMode == COLOR_MODE_HDR
        } else {
            false
        }

    private fun isInversionModeEnabled(context: Context): Boolean {
        val accessibilityEnabled =
            try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED,
                )
            } catch (e: Settings.SettingNotFoundException) {
                Settings.System.getInt(context.contentResolver, "high_contrast", 0)
            }

        return accessibilityEnabled == 1
    }

    private fun getMathFingerprint(): String =
        JSONObject()
            .apply {
                put("acos", acos(0.12312423423423424))
                put("acosh", acosh(1e308))
                put("asin", asin(0.12312423423423424))
                put("asinh", asinh(1.0))
                put("atan", atan(0.5))
                put("atanh", atanh(0.5))
                put("sin", sin(-1e300))
                put("sinh", sinh(1.0))
                put("cos", cos(10.000000000123))
                put("cosh", cosh(1.0))
                put("tan", tan(-1e300))
                put("tanh", tanh(1.0))
                put("exp", exp(1.0))
                put("expm1", expm1(1.0))
                put("log1p", ln1p(10.0))
                put("powPI", (PI).pow(-100.0))
            }.toString()
            .encodeToByteArray()
            .toSha1Base64()

    private fun checkDangerousPackages(context: Context): Boolean {
        val packageManager: PackageManager = context.packageManager

        return packageNames
            .mapNotNull {
                try {
                    packageManager.getPackageInfo(it, PackageManager.GET_META_DATA)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }.isNotEmpty()
    }

    private fun checkDebuggable(context: Context): Boolean =
        try {
            val flagsCheck =
                context.applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

            BuildConfig.DEBUG || Debug.isDebuggerConnected() || flagsCheck
        } catch (e: Exception) {
            Log.e(KEYRI_KEY, e.message, e)
            false
        }

    private fun checkInstallationSource(context: Context): String? {
        val packageManager = context.packageManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            packageManager.getInstallerPackageName(context.packageName)
        }
    }

    private fun getPackageDetails(context: Context): PackageDetails? =
        try {
            val packageManager = context.packageManager

            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.PackageInfoFlags.of(0),
                    )
                } else {
                    packageManager.getPackageInfo(context.packageName, 0)
                }

            val appVersionName = packageInfo.versionName

            val appVersionCode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    packageInfo.versionCode.toLong()
                }

            val appSignatures = getApplicationSignature(context)

            PackageDetails(appVersionName, appVersionCode, appSignatures)
        } catch (e: Exception) {
            Log.d(KEYRI_KEY, e.message, e)
            null
        }

    private fun getResolutionInfo(context: Context): DisplayMetrics {
        val windowManager =
            context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        windowManager.defaultDisplay.getMetrics(displayMetrics)

        return displayMetrics
    }

    @SuppressLint("MissingPermission")
    private fun getCarrierInfo(context: Context): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                telephonyManager?.simSpecificCarrierIdName?.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getCPUName(): String? {
        var result: String? = null

        try {
            val scanner = Scanner(File("/proc/cpuinfo"))

            while (scanner.hasNextLine()) {
                val values = scanner.nextLine().split(": ")

                if (values.size > 1) {
                    val key = values[0].trim { it <= ' ' }

                    if (key == "Hardware" || key == "model name") {
                        result = values[1].trim { it <= ' ' }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            Log.d(KEYRI_KEY, e.message, e)
        }

        return result
    }

    private fun getKernelVersion(): String? {
        var result: String? = null

        try {
            val p = Runtime.getRuntime().exec("uname -a")
            val stdInput = BufferedReader(InputStreamReader(p.inputStream))
            var output: String? = ""
            var s: String?

            while (stdInput.readLine().also { s = it } != null) {
                output += s
            }

            result = output
        } catch (e: IOException) {
            Log.d(KEYRI_KEY, e.message, e)
        }

        return result
    }

    private fun getSupportedCodecs(): List<String> = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.map { it.name }

    private fun getAvailableLocales(): List<String> = Locale.getAvailableLocales().map { it.displayName }

    private fun getSystemApps(context: Context): List<String> =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getInstalledApplications(
                    PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_SYSTEM_ONLY.toLong()),
                )
            } else {
                context.packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { it.flags and ApplicationInfo.FLAG_SYSTEM != 0 }
            }.map { it.name }
        } catch (e: Exception) {
            Log.d(KEYRI_KEY, e.message, e)
            listOf()
        }

    private fun getTotalRAMMemory(context: Context): String {
        val activityManager =
            context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        activityManager.getMemoryInfo(memoryInfo)

        return "${(memoryInfo.totalMem / (1024 * 1024 * 1024)).toInt()} GB"
    }

    private fun getTotalStorageMemory(): String {
        val stat = StatFs(Environment.getDataDirectory().path)

        return "${((stat.blockCountLong * stat.blockSizeLong) / (1024 * 1024 * 1024)).toInt()}"
    }

    private fun getApplicationSignature(context: Context): List<String>? =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val sig =
                    context.packageManager
                        .getPackageInfo(
                            context.packageName,
                            PackageManager.GET_SIGNING_CERTIFICATES,
                        ).signingInfo
                if (sig.hasMultipleSigners()) {
                    sig.apkContentsSigners
                } else {
                    sig.signingCertificateHistory
                }
            } else {
                context.packageManager
                    .getPackageInfo(
                        context.packageName,
                        PackageManager.GET_SIGNATURES,
                    ).signatures
            }.map {
                it.toByteArray().toSha1Base64()
            }
        } catch (e: Exception) {
            Log.d(KEYRI_KEY, e.message, e)
            null
        }

    private fun getDeviceName(context: Context): String =
        try {
            val contentResolver = context.contentResolver

            var userDeviceName: String? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    Settings.Global.getString(contentResolver, Settings.Global.DEVICE_NAME)
                } else {
                    null
                }

            if (userDeviceName == null) {
                userDeviceName = Settings.Secure.getString(contentResolver, "bluetooth_name")
            }

            userDeviceName ?: getDeviceModel()
        } catch (e: Exception) {
            getDeviceModel()
        }

    private fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            model.uppercase()
        } else {
            manufacturer.uppercase() + " " + model
        }
    }

    private val packageNames =
        listOf(
            "com.zachspong.temprootremovejb",
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root.global",
            "com.alephzain.framaroot",
            "com.koushikdutta.rommanager",
            "com.koushikdutta.rommanager.license",
            "com.dimonvideo.luckypatcher",
            "com.chelpus.lackypatch",
            "com.ramdroid.appquarantine",
            "com.ramdroid.appquarantinepro",
            "com.android.vending.billing.InAppBillingService.COIN",
            "com.android.vending.billing.InAppBillingService.LUCK",
            "com.chelpus.luckypatcher",
            "com.blackmartalpha",
            "org.blackmart.market",
            "com.allinone.free",
            "com.repodroid.app",
            "org.creeplays.hack",
            "com.baseappfull.fwd",
            "com.zmapp",
            "com.dv.marketmod.installer",
            "org.mobilism.android",
            "com.android.wp.net.log",
            "com.android.camera.update",
            "cc.madkite.freedom",
            "com.solohsu.android.edxp.manager",
            "org.meowcat.edxposed.manager",
            "com.xmodgame",
            "com.cih.game_cih",
            "com.charles.lpoqasert",
            "catch_.me_.if_.you_.can_",
            "gb.artfilter.tenvarnist",
            "de.nineergysh.quickarttwo",
            "gb.painnt.moonlightingnine",
            "gb.twentynine.redaktoridea",
            "de.photoground.twentysixshot",
            "de.xnano.photoexifeditornine",
            "de.hitopgop.sixtyeightgx",
            "de.sixtyonecollice.cameraroll",
            "de.instgang.fiftyggfife",
            "de.fiftyninecamera.rollredactor",
            "gb.crazykey.sevenboard",
            "com.neonthemekeyboard.app",
            "com.androidneonkeyboard.app",
            "com.cachecleanereasytool.app",
            "com.fancyanimatedbattery.app",
            "com.fastcleanercashecleaner.app",
            "com.rockskinthemes.app",
            "com.funnycallercustomtheme.app",
            "com.callercallwallpaper.app",
            "com.mycallcustomcallscrean.app",
            "com.mycallcallpersonalization.app",
            "com.caller.theme.slow",
            "com.callertheme.firstref",
            "com.funnywallpapaerslive.app",
            "de.andromo.ssfiftylivesixcc",
            "com.newscrean4dwallpapers.app",
            "de.stockeighty.onewallpapers",
            "com.notesreminderslists.app",
        )

    data class PackageDetails(
        val appVersionName: String?,
        val appVersionCode: Long?,
        val appSignatures: List<String>?,
    )

    companion object {
        private const val USER_AGENT_STRING_KEY = "USER_AGENT_STRING_KEY"
    }
}
