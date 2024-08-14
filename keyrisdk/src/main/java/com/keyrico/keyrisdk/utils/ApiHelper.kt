package com.keyrico.keyrisdk.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.keyrico.keyrisdk.converter.ConverterFactory
import com.keyrico.keyrisdk.entity.ErrorResponse
import com.keyrico.keyrisdk.exception.AuthorizationException
import com.keyrico.keyrisdk.exception.KeyriApiException
import com.keyrico.keyrisdk.exception.NetworkException
import com.keyrico.keyrisdk.sec.checkFakeNonKeyriInvocation
import com.keyrico.keyrisdk.services.api.ApiService
import com.keyrico.keyrisdk.services.api.AssociationKeysApiService
import com.keyrico.keyrisdk.services.api.ChecksumApiService
import com.keyrico.keyrisdk.services.api.FraudApiService
import com.keyrico.keyrisdk.telemetry.TelemetryManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

internal suspend fun getApiCallResponseCode(
    blockSwizzleDetection: Boolean,
    call: suspend () -> Response<*>,
): Result<Int> {
    checkFakeNonKeyriInvocation(blockSwizzleDetection)

    return try {
        val code = call.invoke().code()

        Result.success(code)
    } catch (exception: Exception) {
        return Result.failure(exception)
    }
}

internal suspend fun <T : Any> makeApiCall(
    context: Context,
    blockSwizzleDetection: Boolean,
    call: suspend () -> Response<T>,
): Result<T> {
    checkFakeNonKeyriInvocation(blockSwizzleDetection)

    try {
        val response = call.invoke()

        if (!response.isSuccessful) {
            val type = object : TypeToken<ErrorResponse>() {}.type
            val errorResponse: ErrorResponse? =
                Gson().fromJson(response.errorBody()?.charStream(), type)

            val exception = AuthorizationException(errorResponse?.message)

            return Result.failure(exception)
        }

        return try {
            val responseJson = Gson().toJson(response.body())
            val json = JSONObject(responseJson)

            if (json.has("message")) {
                Result.failure(AuthorizationException(json.getString("message")))
            } else if (json.has("error")) {
                json.getJSONObject("error").takeIf { it.has("message") }?.let {
                    val message = it.getString("message") ?: KEYRI_API_ERROR
                    val exception = KeyriApiException(message)

                    Result.failure(exception)
                } ?: Result.failure(AuthorizationException())
            } else {
                response.body()?.let { Result.success(it) } ?: throw AuthorizationException()
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    } catch (e: Exception) {
        val error =
            when (e) {
                is UnknownHostException,
                is SocketTimeoutException,
                is ConnectException,
                -> NetworkException()

                else -> e
            }

        TelemetryManager.sendEvent(context, error)

        return Result.failure(error)
    }
}

internal fun provideApiService(blockSwizzleDetection: Boolean): ApiService {
    return provideApiService(
        "https://prod.api.keyri.com",
        blockSwizzleDetection,
        ApiService::class.java,
    )
}

internal fun provideAssociationKeysApiService(blockSwizzleDetection: Boolean): AssociationKeysApiService {
    return provideApiService(
        "https://api.keyri.co",
        blockSwizzleDetection,
        AssociationKeysApiService::class.java,
    )
}

internal fun provideFraudApiService(blockSwizzleDetection: Boolean): FraudApiService {
    return provideApiService(
        "https://fp.keyri.com",
        blockSwizzleDetection,
        FraudApiService::class.java,
    )
}

internal fun provideChecksumApiService(blockSwizzleDetection: Boolean): ChecksumApiService {
    return provideApiService(
        "https://td.api.keyri.com",
        blockSwizzleDetection,
        ChecksumApiService::class.java,
    )
}

private fun <T> provideApiService(
    baseUrl: String,
    blockSwizzleDetection: Boolean,
    clazz: Class<T>,
): T {
    checkFakeNonKeyriInvocation(blockSwizzleDetection)

    val okHttpClientBuilder = OkHttpClient.Builder()

    okHttpClientBuilder.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)

    HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }.let(okHttpClientBuilder::addInterceptor)

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(ConverterFactory())
        .client(okHttpClientBuilder.build())
        .build()
        .create(clazz)
}

internal const val API_VERSION = "v1"
private const val TIMEOUT = 15L
private const val KEYRI_API_ERROR = "Keyri API error"
