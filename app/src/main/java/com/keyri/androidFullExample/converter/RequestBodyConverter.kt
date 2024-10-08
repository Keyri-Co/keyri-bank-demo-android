package com.keyri.androidFullExample.converter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonWriter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import retrofit2.Converter
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.StandardCharsets.UTF_8

class RequestBodyConverter<T>(
    private var gson: Gson,
    private var adapter: TypeAdapter<T>,
) : Converter<T, RequestBody> {
    override fun convert(value: T): RequestBody {
        val buffer = Buffer()
        val writer: Writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
        val jsonWriter: JsonWriter = gson.newJsonWriter(writer)

        adapter.write(jsonWriter, value)
        jsonWriter.close()

        return buffer
            .readByteString()
            .toRequestBody("application/json; charset=UTF-8".toMediaType())
    }
}
