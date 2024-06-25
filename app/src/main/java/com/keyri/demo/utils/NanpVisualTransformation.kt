package com.keyri.demo.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle

class NanpVisualTransformation : VisualTransformation {

    private val placeholder = "(---) --- - ----"

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(10)
        val formattedText = buildAnnotatedString {
            append("+1 (")
            trimmed.forEachIndexed { index, char ->
                if (index == 3) append(") ")
                if (index == 6) append(" - ")
                append(char)
            }
            if (trimmed.length < 10) {
                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append(placeholder.drop(trimmed.length))
                }
            }
        }

        return TransformedText(formattedText, phoneNumberOffsetTranslator(trimmed.length))
    }

    private fun phoneNumberOffsetTranslator(enteredLength: Int): OffsetMapping {
        return object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when (offset) {
                    0 -> 4 // "+1 (" is 4 characters
                    in 1..3 -> offset + 4 // "+1 (XXX"
                    in 4..6 -> offset + 6 // "+1 (XXX) XXX"
                    in 7..10 -> offset + 8 // "+1 (XXX) XXX-XXXX"
                    else -> 18 // Full formatted length
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when (offset) {
                    in 0..4 -> 0 // "+1 ("
                    in 5..8 -> offset - 4 // "+1 (XXX"
                    in 9..13 -> offset - 6 // "+1 (XXX) XXX"
                    in 14..18 -> offset - 8 // "+1 (XXX) XXX-XXXX"
                    else -> enteredLength // Full entered length
                }
            }
        }
    }
}
