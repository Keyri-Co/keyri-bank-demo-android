package com.keyrico.keyrisdk.backup

import android.app.backup.BackupAgentHelper
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.app.backup.SharedPreferencesBackupHelper
import android.os.ParcelFileDescriptor
import android.util.Log
import com.keyrico.keyrisdk.Keyri.Companion.BACKUP_KEYRI_PREFS
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_KEY
import java.io.File

class KeyriPrefsBackupAgent : BackupAgentHelper() {
    override fun onCreate() {
        val helper = SharedPreferencesBackupHelper(this, BACKUP_KEYRI_PREFS)

        addHelper(KEYRI_PREFS_BACKUP_KEY, helper)
    }

    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput?,
        newState: ParcelFileDescriptor?,
    ) {
        super.onBackup(oldState, data, newState)

        Log.i(KEYRI_KEY, "onBackup")
    }

    override fun onFullBackup(data: FullBackupDataOutput?) {
        super.onFullBackup(data)

        Log.i(KEYRI_KEY, "onFullBackup")
    }

    override fun onRestore(
        data: BackupDataInput?,
        appVersionCode: Int,
        newState: ParcelFileDescriptor?,
    ) {
        super.onRestore(data, appVersionCode, newState)

        Log.i(KEYRI_KEY, "onRestore")
    }

    override fun onRestoreFile(
        data: ParcelFileDescriptor?,
        size: Long,
        destination: File?,
        type: Int,
        mode: Long,
        mtime: Long,
    ) {
        super.onRestoreFile(data, size, destination, type, mode, mtime)

        Log.i(KEYRI_KEY, "onRestoreFile")
    }

    companion object {
        const val KEYRI_PREFS_BACKUP_KEY = "keyri_prefs_backup_key"
    }
}
