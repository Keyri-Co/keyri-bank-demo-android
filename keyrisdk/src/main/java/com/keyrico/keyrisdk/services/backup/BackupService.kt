package com.keyrico.keyrisdk.services.backup

import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_KEY
import com.keyrico.keyrisdk.entity.associationkey.NewAssociationKey
import com.keyrico.keyrisdk.sec.checkFakeNonKeyriInvocation
import com.keyrico.keyrisdk.services.CryptoService.Companion.ECDSA_KEYPAIR
import com.keyrico.keyrisdk.telemetry.TelemetryCodes
import com.keyrico.keyrisdk.telemetry.TelemetryManager
import com.keyrico.keyrisdk.utils.toSha1Base64
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class BackupService(
    private val context: Context,
    private val backupCallbacks: BackupCallbacks,
    private val blockSwizzleDetection: Boolean,
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Keyri.BACKUP_KEYRI_PREFS, AppCompatActivity.MODE_PRIVATE)

    fun addKey(
        alias: String,
        associationKey: String,
    ) {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        sharedPreferences.edit(commit = true) {
            putString(alias, associationKey)
            triggerDataChanged()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun checkBackupAccounts() {
        GlobalScope.launch(Dispatchers.IO) {
            checkBackupAccountsAsync()
        }
    }

    fun removeKey(alias: String, ) {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        sharedPreferences.edit(commit = true) {
            remove(alias)
            triggerDataChanged()
        }
    }

    suspend fun waitForAccountsRestoring() {
        checkBackupAccountsAsync()
    }

    private suspend fun checkBackupAccountsAsync() {
        val accounts = backupCallbacks.onListUniqueAccounts()

        if (accounts.isNotEmpty()) {
            val restoredKeys = getAllPreferencesKeys()

            if (restoredKeys.isNotEmpty()) {
                    restoredKeys.mapNotNull { (username, oldKey) ->
                        val usernameWithoutPrefix = username.removePrefix(ECDSA_KEYPAIR)
                        val newKey = backupCallbacks.onGetAssociationKey(usernameWithoutPrefix)

                        if (newKey != oldKey) {
                            NewAssociationKey(usernameWithoutPrefix, oldKey, newKey)
                        } else {
                            null
                        }
                    }
            }
        } else {
            val keystoreKeysDigestString = getKeystoreKeysDigestString()

            TelemetryManager.sendEvent(context, TelemetryCodes.CHECK_BACKEND_ACCOUNTS_HASH)

            if (!isSnapshotHashActual(keystoreKeysDigestString)) {
                synchronizeDeviceAccounts(keystoreKeysDigestString)
            }
        }
    }

    private fun synchronizeDeviceAccounts(
        keystoreKeysDigestString: String,
    ) {
        TelemetryManager.sendEvent(context, TelemetryCodes.UPDATE_BACKEND_ACCOUNTS)

        setNewSnapshotHash(keystoreKeysDigestString)
    }

    private fun isSnapshotHashActual(actualSnapshotHash: String): Boolean {
        return sharedPreferences.getString(LAST_SNAPSHOT_HASH, null) == actualSnapshotHash
    }

    private fun setNewSnapshotHash(hash: String) {
        sharedPreferences.edit(commit = true) {
            putString(LAST_SNAPSHOT_HASH, hash)
        }
    }

    private suspend fun getKeystoreKeysDigestString(): String {
        val keysString =
            backupCallbacks.onListUniqueAccounts()
                .toSortedMap()
                .map { "${it.key}.${it.value}" }
                .joinToString()
                .toByteArray()

        return keysString.toSha1Base64()
    }

    private fun getAllPreferencesKeys(): Map<String, String?> {
        return sharedPreferences.all
            ?.filter { it.key.contains(ECDSA_KEYPAIR) }
            ?.map { it.key to it.value.toString() }
            ?.associate { it.first to it.second }
            ?: mapOf()
    }

    private fun triggerDataChanged() {
        try {
            BackupManager(context).dataChanged()

            TelemetryManager.sendEvent(
                context,
                TelemetryCodes.ON_DATA_CHANGED_FOR_CLOUD_BACKUP,
                "prefsSize: ${sharedPreferences.all.size}",
            )

            Log.i(KEYRI_KEY, "dataChanged")
        } catch (e: Exception) {
            Log.e(KEYRI_KEY, e.message, e)
        }
    }

    companion object {
        private const val LAST_SNAPSHOT_HASH = "LAST_SNAPSHOT_HASH"
    }
}
