package com.inumaki.chouten.features.Discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.inumaki.chouten.theme.ChoutenTheme

@Composable
fun LoadingList() {
    val startOffset = 20.dp // if (LocalDeviceInfo.current.isTablet) 130.dp else 20.dp

    Column(
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(24.dp)
                .background(ChoutenTheme.colors.container, shape = RoundedCornerShape(6.dp))
                .border(0.5.dp, ChoutenTheme.colors.border, shape = RoundedCornerShape(6.dp))
        )

        LazyRow(
            contentPadding = PaddingValues(start = startOffset, top = 12.dp, bottom = 12.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 0..12) {
                item {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(size = 8.dp))
                                .background(ChoutenTheme.colors.container)
                                .border(
                                    width = 0.5.dp,
                                    color = ChoutenTheme.colors.border,
                                    shape = RoundedCornerShape(size = 8.dp)
                                ),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(ChoutenTheme.colors.overlay)
                                    .border(0.5.dp, ChoutenTheme.colors.border, RoundedCornerShape(percent = 50))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ChoutenTheme.colors.overlay)
                                .border(0.5.dp, ChoutenTheme.colors.border, RoundedCornerShape(percent = 50))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ChoutenTheme.colors.overlay)
                                .border(0.5.dp, ChoutenTheme.colors.border, RoundedCornerShape(percent = 50))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}