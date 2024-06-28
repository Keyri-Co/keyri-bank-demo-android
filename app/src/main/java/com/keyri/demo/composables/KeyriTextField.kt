package com.keyri.demo.composables

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun KeyriTextField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        placeholder = placeholder,
        onValueChange = onValueChange,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
    )
}
