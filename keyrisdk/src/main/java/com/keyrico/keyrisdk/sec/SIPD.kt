@file:Suppress("Deprecation")

package com.keyrico.keyrisdk.sec

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Scanner

internal class SIPD(private val detectionsConfig: KeyriDetectionsConfig) {
    private val blockSwizzleDetection: Boolean = detectionsConfig.blockSwizzleDetection

    init {
        checkFakeNonKeyriInstance(blockSwizzleDetection)
    }

    private val knownRootAppsPackages =
        listOf(
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
        )

    private val knownDangerousAppsPackages =
        listOf(
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
            // TODO: Remove
            "com.facebook.katana",
            "com.facebook.lite",
            "com.facebook.android",
            "com.example.facebook"
        )

    private val suPaths =
        listOf(
            "/data/local/",
            "/data/local/su",
            "/data/local/bin/",
            "/data/local/xbin/",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/sbin/",
            "/sbin/su",
            "/su/bin/",
            "/system/bin/",
            "/system/bin/su",
            "/system/bin/.ext/",
            "/system/bin/failsafe/",
            "/system/bin/failsafe/su",
            "/system/sd/xbin/su",
            "/system/xbin/su",
            "/system/xbin/busybox",
            "/system/sd/xbin/",
            "/system/usr/we-need-root/",
            "/system/xbin/",
            "/cache/",
            "/data/",
            "/dev/",
            "/system/app/Superuser.apk",
            "/system/etc/init.d/99SuperSUDaemon",
            "/dev/com.koushikdutta.superuser.daemon/",
            "/system/xbin/daemonsu",
        )

    private val pathsThatShouldNotBeWritable =
        listOf(
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin",
            "/etc",
        )

    fun checkSIPD(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkSIPD") {
            val blockDangerousAppsDetection = detectionsConfig.blockDangerousAppsDetection
            val blockEmulatorDetection = detectionsConfig.blockEmulatorDetection
            val blockRootDetection = detectionsConfig.blockRootDetection

            (!blockDangerousAppsDetection and checkDangerousApps(context)) or
                    (!blockEmulatorDetection and checkBinaries()) or
                    (!blockEmulatorDetection and checkForDangerousProps()) or
                    (!blockRootDetection and checkForRWPaths()) or
                    (!blockEmulatorDetection and detectTestKeys()) or
                    (!blockRootDetection and (checkSuExists() or checkForMagiskBinary()))
        }
    }

    private fun checkDangerousApps(context: Context): Boolean {
        return returnWithLog("checkDangerousApps") {
            detectRootManagementApps(context) or detectPotentiallyDangerousApps(context)
        }
    }

    private fun checkBinaries(): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkBinaries") {
            checkForBinary(BINARY_SU) or checkForBinary(BINARY_BUSY_BOX)
        }
    }

    private fun detectTestKeys(): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("detectTestKeys") { Build.TAGS?.contains("test-keys") ?: false }
    }

    private fun detectRootManagementApps(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("detectRootManagementApps") {
            isAnyPackageFromListInstalled(context, knownRootAppsPackages)
        }
    }

    private fun detectPotentiallyDangerousApps(context: Context): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("detectPotentiallyDangerousApps") {
            isAnyPackageFromListInstalled(context, knownDangerousAppsPackages)
        }
    }

    private fun checkForMagiskBinary(): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkForMagiskBinary") {
            checkForBinary("magisk")
        }
    }

    private fun checkForBinary(filename: String): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkForBinary") {
            var result = false

            getPaths().forEach { path ->
                if (File(path, filename).exists()) {
                    result = true
                }
            }

            result
        }
    }

    private fun executeAndReturnLines(command: String): Array<String>? {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        try {
            val inputStream = Runtime.getRuntime().exec(command).inputStream ?: return null
            val propVal: String = Scanner(inputStream).useDelimiter("\\A").next()

            return propVal.split("\n".toRegex()).toTypedArray()
        } catch (e: Exception) {
            return null
        }
    }

    private fun isAnyPackageFromListInstalled(
        context: Context,
        packages: List<String>,
    ): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("isAnyPackageFromListInstalled") {
            try {
                val installedApps =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val flag = PackageManager.ApplicationInfoFlags.of(0)

                        context.packageManager.getInstalledApplications(flag)
                    } else {
                        context.packageManager.getInstalledApplications(0)
                    }

                val checkResult = installedApps.any { packages.contains(it.packageName) }

                checkResult
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun checkForDangerousProps(): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkForDangerousProps") {
            val dangerousProps: MutableMap<String, String> = HashMap()

            dangerousProps["ro.debuggable"] = "1"
            dangerousProps["ro.secure"] = "0"

            var result = false
            val lines: Array<String> =
                executeAndReturnLines("getprop") ?: return@returnWithLog false

            lines.forEach { line ->
                dangerousProps.keys.forEach { key ->
                    if (line.contains(key)) {
                        var badValue: String? = dangerousProps[key]
                        badValue = "[$badValue]"

                        if (line.contains(badValue)) {
                            result = true
                        }
                    }
                }
            }

            result
        }
    }

    private fun checkForRWPaths(): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkForRWPaths") {
            var result = false
            val lines: Array<String> = executeAndReturnLines("mount") ?: return@returnWithLog false
            val sdkVersion: Int = Build.VERSION.SDK_INT

            for (line: String in lines) {
                val args: Array<String> = line.split(" ".toRegex()).toTypedArray()

                if ((sdkVersion <= Build.VERSION_CODES.M && args.size < 4) ||
                    (sdkVersion > Build.VERSION_CODES.M && args.size < 6)
                ) {
                    continue
                }

                var mountPoint: String
                var mountOptions: String

                if (sdkVersion > Build.VERSION_CODES.M) {
                    mountPoint = args[2]
                    mountOptions = args[5]
                } else {
                    mountPoint = args[1]
                    mountOptions = args[3]
                }

                for (pathToCheck: String in pathsThatShouldNotBeWritable) {
                    if (mountPoint.equals(pathToCheck, ignoreCase = true)) {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            mountOptions =
                                mountOptions
                                    .replace("(", "")
                                    .replace(")", "")
                        }

                        for (option: String in mountOptions.split(",".toRegex()).toTypedArray()) {
                            if (option.equals("rw", ignoreCase = true)) {
                                result = true
                                break
                            }
                        }
                    }
                }
            }

            result
        }
    }

    private fun checkSuExists(): Boolean {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        return returnWithLog("checkSuExists") {
            var process: Process? = null

            try {
                process = Runtime.getRuntime().exec(arrayOf("which", BINARY_SU))

                BufferedReader(InputStreamReader(process.inputStream)).readLine() != null
            } catch (t: Throwable) {
                false
            } finally {
                process?.destroy()
            }
        }
    }

    private fun getPaths(): Array<String> {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        val paths: ArrayList<String> = ArrayList(suPaths)
        val sysPaths = System.getenv("PATH")

        if (sysPaths == null || "" == sysPaths) {
            return paths.toArray(arrayOfNulls(0))
        }

        for (path in sysPaths.split(":".toRegex()).toTypedArray()) {
            var newPath = ""

            if (!path.endsWith("/")) {
                newPath = "$path/"
            }

            if (!paths.contains(newPath)) {
                paths.add(newPath)
            }
        }

        return paths.toArray(arrayOfNulls(0))
    }

    private fun returnWithLog(
        methodName: String,
        block: () -> Boolean,
    ): Boolean {
        val result = block()

        if (result) {
            Log.e(Keyri.KEYRI_KEY, methodName)
        }

        return result
    }

    companion object {
        private const val BINARY_SU = "su"
        private const val BINARY_BUSY_BOX = "busybox"
    }
}
