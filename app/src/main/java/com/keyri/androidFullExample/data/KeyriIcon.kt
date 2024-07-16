package com.keyri.androidFullExample.data

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.keyri.androidFullExample.theme.textFieldUnfocusedColor

@Composable
fun KeyriIcon(
    modifier: Modifier,
    @DrawableRes iconResId: Int,
    iconTint: Color,
    iconSizeFraction: Float = 0.3F,
) {
    Box(
        modifier =
            modifier
                .size(110.dp)
                .background(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    textFieldUnfocusedColor.copy(alpha = 0.4F),
                                    Color.Transparent,
                                ),
                        ),
                ).padding(bottom = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            modifier =
                Modifier
                    .padding()
                    .size(90.dp)
                    .background(Color.Transparent),
        ) {
            Box(
                modifier = Modifier.background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(iconSizeFraction),
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = iconTint,
                )
            }
        }
    }
}
