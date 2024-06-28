package com.keyri.demo.utils

import android.content.Context
import android.content.ContextWrapper
import android.util.Patterns
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.keyri.demo.data.KeyriProfiles
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun CharSequence.isValidEmail() =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun CharSequence.isValidPhoneNumber() =
    !isNullOrEmpty() && startsWith("+1") && length == 10

fun Context.getActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

val Context.keyriProfilesDataStore: DataStore<KeyriProfiles> by dataStore(
    fileName = "keyri_profiles.pb",
    serializer = KeyriProfilesSerializer
)

object KeyriProfilesSerializer : Serializer<KeyriProfiles> {
    override val defaultValue: KeyriProfiles = KeyriProfiles(null, listOf())

    override suspend fun readFrom(input: InputStream): KeyriProfiles = withContext(Dispatchers.IO) {
        try {
            Json.decodeFromString<KeyriProfiles>(input.readBytes().decodeToString())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }
    }

    override suspend fun writeTo(
        t: KeyriProfiles,
        output: OutputStream
    ) = withContext(Dispatchers.IO) {
        output.write(Json.encodeToString(t).encodeToByteArray())
    }
}

fun NavHostController.navigateWithPopUp(toRoute: String, fromRoute: String) {
    this.navigate(toRoute) {
        popUpTo(fromRoute) {
            inclusive = true
        }
    }
}
