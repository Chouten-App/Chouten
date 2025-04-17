package com.inumaki.chouten.features.Discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingCarousel() {
    val state = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = state)

    val startOffset = 40.dp // if (LocalDeviceInfo.current.isTablet) 130.dp else 40.dp

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(620.dp),
        verticalAlignment = Alignment.CenterVertically,
        state = state,
        contentPadding = PaddingValues(top = 120.dp, start = startOffset, end = 40.dp),
        flingBehavior = snapFlingBehavior,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        for (i in 0..5) {
            item {
                LoadingCarouselCard()
            }
        }
    }
}