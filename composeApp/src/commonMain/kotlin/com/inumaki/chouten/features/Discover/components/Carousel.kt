package com.inumaki.chouten.features.Discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.inumaki.chouten.Models.DiscoverSection
import com.inumaki.chouten.components.NetworkImage
import com.inumaki.chouten.theme.LocalDeviceInfo

@Composable
fun Modifier.conditional(condition : Boolean, modifier : @Composable Modifier.() -> Modifier) : Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

@Composable
fun Carousel(data: DiscoverSection, showBanner: Boolean = true) {
    val state = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val index by remember { derivedStateOf { state.firstVisibleItemIndex } }

    val startOffset = if (LocalDeviceInfo.current.isTablet) 130.dp else 40.dp

    Box {
        if (showBanner) {
            NetworkImage(
                url = data.list[index + 1].banner ?: data.list[index].poster,
                modifier = Modifier
                    .fillMaxSize()
                    .height(620.dp)
                    .blur(20.dp)
            )
        }


        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
                .conditional(showBanner) {
                    background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x790C0C0C),
                                Color(0xFF0C0C0C)
                            )
                        )
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
            state = state,
            contentPadding = PaddingValues(top = 120.dp, start = startOffset, end = 40.dp),
            flingBehavior = snapFlingBehavior,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(data.list) {
                CarouselCard(data = it)
            }
        }
    }
}