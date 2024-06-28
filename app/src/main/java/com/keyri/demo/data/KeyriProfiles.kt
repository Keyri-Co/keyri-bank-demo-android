package com.keyri.demo.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyriProfiles(val currentProfile: String?, val profiles: List<KeyriProfile>)
