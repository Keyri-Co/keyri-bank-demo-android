package com.keyrico.keyrisdk.services.api

import com.keyrico.keyrisdk.entity.associationkey.AccountResponse
import com.keyrico.keyrisdk.entity.associationkey.AssociationKeysHashCheck
import com.keyrico.keyrisdk.entity.associationkey.ChangeAssociationKeysRequest
import com.keyrico.keyrisdk.entity.associationkey.KeyriResponse
import com.keyrico.keyrisdk.entity.associationkey.RemoveAssociationKeysRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

internal interface AssociationKeysApiService {
    @POST("users/change-association-key")
    suspend fun changeAssociationKeys(
        @Header("x-api-key") apiKey: String,
        @Header("deviceId") deviceId: String?,
        @Header("cryptocookie") cryptoCookie: String?,
        @Body request: ChangeAssociationKeysRequest,
    ): Response<Unit>

    @GET("users/send-association-keys-snapshot-hash")
    suspend fun checkAssociationKeysSnapshotHash(
        @Header("x-api-key") apiKey: String,
        @Header("deviceId") deviceId: String?,
        @Header("cryptocookie") cryptoCookie: String?,
        @Query("hash") hash: String,
    ): Response<KeyriResponse<AssociationKeysHashCheck>>

    @PATCH("users/remove-association-key")
    suspend fun removeAssociationKey(
        @Header("x-api-key") apiKey: String,
        @Header("deviceid") deviceId: String?,
        @Header("associationkey") associationKey: String,
        @Header("cryptocookie") cryptoCookie: String?,
        @Body request: RemoveAssociationKeysRequest,
    ): Response<Unit>

    @GET("users/get-association-keys")
    suspend fun getAssociationKeys(
        @Header("x-api-key") apiKey: String,
        @Header("deviceId") deviceId: String?,
        @Header("cryptocookie") cryptoCookie: String?,
    ): Response<KeyriResponse<List<AccountResponse>>>

    @POST("users/synchronize-device-accounts")
    suspend fun synchronizeDeviceAccounts(
        @Header("x-api-key") apiKey: String,
        @Header("deviceId") deviceId: String?,
        @Header("cryptocookie") cryptoCookie: String?,
        @Body request: ChangeAssociationKeysRequest,
    ): Response<Unit>
}
