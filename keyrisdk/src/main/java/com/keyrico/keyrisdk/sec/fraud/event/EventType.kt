package com.keyrico.keyrisdk.sec.fraud.event

import androidx.annotation.Keep
import org.json.JSONObject

/**
 * The `EventType` class allows to select event type for fingerprinting and provide metadata.
 */
@Suppress("unused")
class EventType private constructor(val name: String, var metadata: JSONObject? = null) {
    @Keep
    companion object {
        @JvmOverloads
        fun visits(metadata: JSONObject? = null) = EventType("visits", metadata)

        @JvmOverloads
        fun login(metadata: JSONObject? = null) = EventType("login", metadata)

        @JvmOverloads
        fun signup(metadata: JSONObject? = null) = EventType("signup", metadata)

        @JvmOverloads
        fun attachNewDevice(metadata: JSONObject? = null) = EventType("attach_new_device", metadata)

        @JvmOverloads
        fun emailChange(metadata: JSONObject? = null) = EventType("email_change", metadata)

        @JvmOverloads
        fun profileUpdate(metadata: JSONObject? = null) = EventType("profile_update", metadata)

        @JvmOverloads
        fun passwordReset(metadata: JSONObject? = null) = EventType("password_reset", metadata)

        @JvmOverloads
        fun withdrawal(metadata: JSONObject? = null) = EventType("withdrawal", metadata)

        @JvmOverloads
        fun deposit(metadata: JSONObject? = null) = EventType("deposit", metadata)

        @JvmOverloads
        fun purchase(metadata: JSONObject? = null) = EventType("purchase", metadata)

        @JvmOverloads
        fun custom(
            name: String,
            metadata: JSONObject? = null,
        ) = EventType(name, metadata)
    }
}
