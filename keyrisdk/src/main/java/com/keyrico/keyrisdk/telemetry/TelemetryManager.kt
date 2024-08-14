package com.keyrico.keyrisdk.telemetry

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.JsonObject
import com.keyrico.keyrisdk.BuildConfig
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_KEY
import com.keyrico.keyrisdk.Keyri.Companion.KEYRI_PLATFORM
import com.keyrico.keyrisdk.converter.ConverterFactory
import com.keyrico.keyrisdk.utils.API_VERSION
import com.keyrico.keyrisdk.utils.getCorrectedTimestampSeconds
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

internal object TelemetryManager {
    private const val TIMEOUT = 15L

    var lastSessionId: String? = null
    var appKey: String? = null
    var apiKey: String? = null
    var deviceID: String? = null

    private val telemetryService by lazy(::provideTelemetryService)

    private val exceptionHandler =
        CoroutineExceptionHandler { _, e ->
            logError(e)
        }

    fun sendEvent(
        context: Context,
        eventCode: TelemetryCodes,
        payload: String? = null,
    ) {
        sendEvent(context, eventCode, null, payload)
    }

    fun sendEvent(
        context: Context,
        error: Throwable,
    ) {
        sendEvent(context, null, error)
    }

    fun sendEvent(
        context: Context,
        eventCode: TelemetryCodes? = null,
        error: Throwable? = null,
        payload: String? = null,
    ) {
        try {
            CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                telemetryService.sendEvent(
                    lastSessionId ?: "",
                    createTelemetryObject(context, eventCode, error, payload),
                )
            }
        } catch (e: Exception) {
            logError(e)
        }
    }

    private suspend fun createTelemetryObject(
        context: Context,
        eventCode: TelemetryCodes?,
        error: Throwable?,
        payload: String?,
    ): JsonObject {
        return JsonObject().apply {
            addProperty("platform", KEYRI_PLATFORM)
            addProperty("sdkVersion", BuildConfig.VERSION)
            addProperty("packageName", context.packageName)
            addProperty("apiVersion", API_VERSION)
            addProperty("osVersion", Build.VERSION.SDK_INT)
            addProperty("deviceType", getDeviceName())
            addProperty("appKey", appKey)
            addProperty("apiKey", apiKey)
            addProperty("deviceID", deviceID)

            eventCode?.let { addProperty("eventCode", it.codeName) }
            payload?.let { addProperty("payload", it) }

            addProperty("status", if (error == null) "success" else "error")
            addProperty("timestamp", getCorrectedTimestampSeconds(context))

            error?.let {
                addProperty("message", it.message)
                addProperty("stacktrace", it.stackTraceToString())
            }
        }
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            model.uppercase()
        } else {
            manufacturer.uppercase() + " " + model
        }
    }

    private fun provideTelemetryService(): TelemetryService {
        val okHttpClientBuilder = OkHttpClient.Builder()

        okHttpClientBuilder.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)

        return Retrofit.Builder()
            .baseUrl("https://prod.api.keyri.com")
            .addConverterFactory(ConverterFactory())
            .client(okHttpClientBuilder.build())
            .build()
            .create(TelemetryService::class.java)
    }

    private fun logError(error: Throwable) {
        Log.e(KEYRI_KEY, "Failed to send telemetry event", error)
    }

    interface TelemetryService {
        @POST("api/logs/android/events")
        suspend fun sendEvent(
            @Header("x-session-id") sessionId: String,
            @Body request: JsonObject,
        ): Response<Unit>
    }
}
