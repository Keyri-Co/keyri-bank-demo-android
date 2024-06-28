package com.keyri.demo.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun KeyriButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    progress: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.primary,
    disabledTextColor: Color = Color.Companion.Unspecified,
    containerColor: Color = Color.Companion.Unspecified,
    disabledContainerColor: Color = Color.Companion.Unspecified,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    disabledBorderColor: Color = Color.Companion.Unspecified,
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) borderColor else disabledBorderColor
        ),
        colors = ButtonDefaults.outlinedButtonColors()
            .copy(
                containerColor = containerColor,
                disabledContainerColor = disabledContainerColor,
            ),
        enabled = enabled,
        onClick = onClick,
    ) {
        if (progress) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(32.dp),
                color = textColor
            )
        } else {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                text = text,
                color = if (enabled) textColor else disabledTextColor
            )
        }
    }
}
