package com.keyri.androidFullExample.data

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.keyri.androidFullExample.theme.textFieldUnfocusedColor

@Composable
fun KeyriIcon(modifier: Modifier = Modifier, @DrawableRes iconResId: Int, iconTint: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(shape = RoundedCornerShape(percent = 50), color = Color.White)
            .shadow(
                elevation = 2.dp,
//                ambientColor = textFieldUnfocusedColor,
//                spotColor = textColor,
//                shape = RoundedCornerShape(percent = 50)
            )
    ) {
        Icon(
            modifier = Modifier
                .matchParentSize()
                .padding(20.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(percent = 50)),
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconTint
        )
    }
}
