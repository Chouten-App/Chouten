package com.inumaki.chouten.features.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.inumaki.chouten.Models.DiscoverSection
import com.inumaki.chouten.components.NetworkImage

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

    val startOffset = 40.dp // if (LocalDeviceInfo.current.isTablet) 130.dp else 40.dp

    // "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8YWJzdHJhY3R8ZW58MHx8MHx8fDA%3D"
    Box {
        if (showBanner) {
            NetworkImage(
                url = data.list[index].banner ?: data.list[index].poster,
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