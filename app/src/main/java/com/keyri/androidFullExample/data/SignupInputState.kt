package com.keyri.androidFullExample.data

import androidx.compose.ui.text.input.TextFieldValue

data class SignupInputState(
    val name: String = "",
    val email: String = "",
    val mobile: TextFieldValue = TextFieldValue(text = ""),
)
