package com.keyri.androidFullExample.data

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun KeyriIcon(modifier: Modifier = Modifier, @DrawableRes iconResId: Int) {
    Icon(
        modifier = modifier
            .size(180.dp)
            .background(Color.White, shape = CircleShape)
            .shadow(8.dp),
        painter = painterResource(id = iconResId),
        contentDescription = null
    )
}
