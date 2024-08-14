package com.keyrico.keyrisdk.converter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class ConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val gson = Gson()

        return ResponseBodyConverter(gson, getAdapter(gson, type))
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody> {
        val gson = Gson()

        return RequestBodyConverter(gson, getAdapter(gson, type))
    }

    private fun getAdapter(
        gson: Gson,
        type: Type,
    ) = gson.getAdapter(TypeToken.get(type))
}
