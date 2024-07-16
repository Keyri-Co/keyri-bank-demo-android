package com.keyri.androidFullExample.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyriProfiles(
    val currentProfile: String?,
    val profiles: List<KeyriProfile>,
)
