package com.keyrico.keyrisdk.services.backup

interface BackupCallbacks {
    suspend fun onGetAssociationKey(publicUserId: String): String

    suspend fun onListUniqueAccounts(): Map<String, String>

    suspend fun onCreateNewKey(publicUserId: String): String
}
