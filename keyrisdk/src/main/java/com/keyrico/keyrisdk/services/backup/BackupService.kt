package com.keyrico.keyrisdk.services.backup

import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.Keyri.Companion.ANON_USER
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_KEY
import com.keyrico.keyrisdk.entity.associationkey.ChangeAssociationKeysRequest
import com.keyrico.keyrisdk.entity.associationkey.NewAssociationKey
import com.keyrico.keyrisdk.entity.associationkey.RemoveAssociationKeysRequest
import com.keyrico.keyrisdk.sec.checkFakeNonKeyriInvocation
import com.keyrico.keyrisdk.services.CryptoService.Companion.ECDSA_KEYPAIR
import com.keyrico.keyrisdk.telemetry.TelemetryCodes
import com.keyrico.keyrisdk.telemetry.TelemetryManager
import com.keyrico.keyrisdk.utils.getDeviceId
import com.keyrico.keyrisdk.utils.makeApiCall
import com.keyrico.keyrisdk.utils.provideAssociationKeysApiService
import com.keyrico.keyrisdk.utils.toSha1Base64
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class BackupService(
    private val context: Context,
    private val appKey: String,
    private val backupCallbacks: BackupCallbacks,
    private val blockSwizzleDetection: Boolean,
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Keyri.BACKUP_KEYRI_PREFS, AppCompatActivity.MODE_PRIVATE)

    private val associationKeysApiService = provideAssociationKeysApiService(blockSwizzleDetection)

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

    suspend fun removeKey(
        alias: String,
        key: String,
    ) {
        checkFakeNonKeyriInvocation(blockSwizzleDetection)

        sharedPreferences.edit(commit = true) {
            remove(alias)
            triggerDataChanged()
        }

        makeApiCall(context, blockSwizzleDetection) {
            associationKeysApiService.removeAssociationKey(
                appKey,
                context.getDeviceId(blockSwizzleDetection),
                key,
                backupCallbacks.onGetAssociationKey(ANON_USER),
                RemoveAssociationKeysRequest(alias.removePrefix(ECDSA_KEYPAIR)),
            )
        }
    }

    suspend fun waitForAccountsRestoring() {
        checkBackupAccountsAsync()
    }

    private suspend fun checkBackupAccountsAsync() {
        val accounts = backupCallbacks.onListUniqueAccounts()
        val deviceId = context.getDeviceId(blockSwizzleDetection)

        if (accounts.isNotEmpty()) {
            val restoredKeys = getAllPreferencesKeys()

            if (restoredKeys.isNotEmpty()) {
                val newAssociationKeys =
                    restoredKeys.mapNotNull { (username, oldKey) ->
                        val usernameWithoutPrefix = username.removePrefix(ECDSA_KEYPAIR)
                        val newKey = backupCallbacks.onGetAssociationKey(usernameWithoutPrefix)

                        if (newKey != oldKey) {
                            NewAssociationKey(usernameWithoutPrefix, oldKey, newKey)
                        } else {
                            null
                        }
                    }

                if (newAssociationKeys.isEmpty()) {
                    return
                } else {
                    TelemetryManager.sendEvent(context, TelemetryCodes.PREFS_KEYS_RESTORED)

                    makeApiCall(context, blockSwizzleDetection) {
                        associationKeysApiService.changeAssociationKeys(
                            appKey,
                            deviceId,
                            backupCallbacks.onGetAssociationKey(ANON_USER),
                            ChangeAssociationKeysRequest(newAssociationKeys, deviceId),
                        )
                    }
                }
            } else {
                TelemetryManager.sendEvent(context, TelemetryCodes.GET_BACKEND_ACCOUNTS)

                fetchBackendKeys()
            }
        } else {
            val keystoreKeysDigestString = getKeystoreKeysDigestString()

            TelemetryManager.sendEvent(context, TelemetryCodes.CHECK_BACKEND_ACCOUNTS_HASH)

            if (!isSnapshotHashActual(keystoreKeysDigestString)) {
                makeApiCall(context, blockSwizzleDetection) {
                    associationKeysApiService.checkAssociationKeysSnapshotHash(
                        appKey,
                        deviceId,
                        backupCallbacks.onGetAssociationKey(ANON_USER),
                        keystoreKeysDigestString,
                    )
                }.takeIf { it.isSuccess }?.getOrNull()?.data?.takeIf { !it.identical }
                    ?.let { associationHashCheck ->
                        if (associationHashCheck.accountsCount > 0) {
                            fetchBackendKeys()
                        } else {
                            val newAssociationKeyAccounts =
                                accounts.map { (username, key) ->
                                    NewAssociationKey(
                                        username = username,
                                        oldKey = null,
                                        newKey = key,
                                    )
                                }

                            synchronizeDeviceAccounts(
                                newAssociationKeyAccounts,
                                keystoreKeysDigestString,
                            )
                        }
                    }
            }
        }
    }

    private suspend fun fetchBackendKeys() {
        makeApiCall(context, blockSwizzleDetection) {
            associationKeysApiService.getAssociationKeys(
                appKey,
                context.getDeviceId(blockSwizzleDetection),
                backupCallbacks.onGetAssociationKey(ANON_USER),
            )
        }.takeIf { it.isSuccess }?.getOrNull()?.data?.let { accounts ->
            val newAssociationKeyAccounts =
                withContext(Dispatchers.Main) {
                    accounts.map { it.email }.map { publicUserId ->
                        NewAssociationKey(
                            username = publicUserId,
                            oldKey = null,
                            newKey = backupCallbacks.onCreateNewKey(publicUserId),
                        )
                    }
                }

            val keystoreKeysDigestString =
                withContext(Dispatchers.Main) {
                    getKeystoreKeysDigestString()
                }

            synchronizeDeviceAccounts(
                newAssociationKeyAccounts,
                keystoreKeysDigestString,
            )
        }
    }

    private suspend fun synchronizeDeviceAccounts(
        newAssociationKeyAccounts: List<NewAssociationKey>,
        keystoreKeysDigestString: String,
    ) {
        TelemetryManager.sendEvent(context, TelemetryCodes.UPDATE_BACKEND_ACCOUNTS)

        makeApiCall(context, blockSwizzleDetection) {
            associationKeysApiService.synchronizeDeviceAccounts(
                appKey,
                context.getDeviceId(blockSwizzleDetection),
                backupCallbacks.onGetAssociationKey(ANON_USER),
                ChangeAssociationKeysRequest(
                    newAssociationKeyAccounts,
                    context.getDeviceId(blockSwizzleDetection),
                ),
            )
        }.takeIf { it.isSuccess }?.getOrNull().let {
            setNewSnapshotHash(keystoreKeysDigestString)
        }
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
