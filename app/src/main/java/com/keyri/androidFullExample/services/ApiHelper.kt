package com.keyri.androidFullExample.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.keyri.androidFullExample.converter.ConverterFactory
import com.keyri.androidFullExample.services.entities.responses.ErrorResponse
import com.keyrico.keyrisdk.exception.AuthorizationException
import com.keyrico.keyrisdk.exception.KeyriApiException
import com.keyrico.keyrisdk.exception.NetworkException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

suspend fun <T : Any> makeApiCall(call: suspend () -> Response<T>): Result<T> {
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

            if (json.has("error")) {
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

        return Result.failure(error)
    }
}

fun provideApiService(): ApiService {
    val okHttpClientBuilder = OkHttpClient.Builder()

    okHttpClientBuilder
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)

    HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }.let(okHttpClientBuilder::addInterceptor)

    return Retrofit
        .Builder()
        .baseUrl("https://app-demo-api.keyri.com")
        .addConverterFactory(ConverterFactory())
        .client(okHttpClientBuilder.build())
        .build()
        .create(ApiService::class.java)
}

fun provideRiskApiService(): RiskApiService {
    val okHttpClientBuilder = OkHttpClient.Builder()

    okHttpClientBuilder
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)

    HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }.let(okHttpClientBuilder::addInterceptor)

    return Retrofit
        .Builder()
        .baseUrl("https://keyri-firebase-passkeys.vercel.app")
        .addConverterFactory(ConverterFactory())
        .client(okHttpClientBuilder.build())
        .build()
        .create(RiskApiService::class.java)
}

fun provideTestApiService(): TestApiService {
    val okHttpClientBuilder = OkHttpClient.Builder()

    okHttpClientBuilder
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)

    HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }.let(okHttpClientBuilder::addInterceptor)

    return Retrofit
        .Builder()
        .baseUrl("https://app-demo-api.keyri.com")
        .addConverterFactory(ConverterFactory())
        .client(okHttpClientBuilder.build())
        .build()
        .create(TestApiService::class.java)
}

private const val TIMEOUT = 15L
private const val KEYRI_API_ERROR = "Keyri API error"
