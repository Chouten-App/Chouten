package com.inumaki.chouten.features.Discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.inumaki.chouten.theme.ChoutenTheme

@Composable
fun LoadingCarouselCard() {
    Box(
        modifier = Modifier
            .height(440.dp)
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, ChoutenTheme.colors.border, shape = RoundedCornerShape(20.dp))
            .background(ChoutenTheme.colors.container)
    )
}