package com.inumaki.chouten.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ChCircleButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    sizeModifier: Float? = null,
    onTap: () -> Unit?
) {
    Box(
        modifier = modifier
            .width(32.dp)
            .height(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF272727))
            .border(
                width = 0.5.dp,
                color = Color(0xFF3B3B3B),
                shape = CircleShape
            )
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.width(16.dp).scale(sizeModifier ?: 1.0f),
            imageVector = icon,
            contentDescription = "",
            tint = Color(0xFFD4D4D4)
        )
    }
}