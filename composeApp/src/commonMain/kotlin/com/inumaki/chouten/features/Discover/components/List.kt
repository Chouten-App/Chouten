package com.inumaki.chouten.features.Discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inumaki.relaywasm.models.DiscoverData
import com.inumaki.chouten.components.NetworkImage
import com.inumaki.chouten.theme.ChoutenTheme
import com.inumaki.chouten.theme.LocalDeviceInfo

@Composable
fun List(title: String, list: List<DiscoverData>) {
    val startOffset = if (LocalDeviceInfo.current.isTablet) 130.dp else 20.dp

    Column(
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = startOffset, end = 20.dp),
            text = title,
            color = Color(0xFFD4D4D4),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            contentPadding = PaddingValues(start = startOffset, top = 12.dp, bottom = 12.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = list) { item ->
                Column {
                    Box(
                        modifier = Modifier
                            .clickable {
                                println("Clicked on ${item.url}")

                                /*
                                val encodedUrl = URLEncoder.encode(item.url, "UTF-8")
                                // open info view with data.url
                                navController.navigate(
                                    "InfoView/${encodedUrl}"
                                )
                                 */
                            },
                        contentAlignment = Alignment.TopEnd
                    ) {
                        NetworkImage(
                            url = item.poster,
                            modifier = Modifier.width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(size = 8.dp))
                                .background(Color(0xFF171717))
                                .border(
                                    width = 0.5.dp,
                                    color = Color(0xFF3B3B3B),
                                    shape = RoundedCornerShape(size = 8.dp)
                                )
                        )

                        if (item.indicator != null ) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                            ) {
                                Text(
                                    item.indicator ?: "",
                                    fontSize = 10.sp,
                                    lineHeight = 1.1.em,
                                    color = ChoutenTheme.colors.fg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(Color(0xFF272727))
                                        .border(0.5.dp, Color(0xFF3B3B3B), RoundedCornerShape(percent = 50))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                    }

                    Text(
                        item.titles.primary,
                        fontSize = 12.sp,
                        lineHeight = 1.1.em,
                        maxLines = 2,
                        color = ChoutenTheme.colors.fg,
                        modifier = Modifier
                            .width(120.dp)
                            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp, top = 4.dp)
                    )

                    Text(
                        "${item.current ?: "~"}/${item.total ?: "~"}",
                        fontSize = 10.sp,
                        lineHeight = 1.1.em,
                        color = ChoutenTheme.colors.fg,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                            .offset(y = (-2).dp)
                            .alpha(0.7f)
                    )
                }
            }
        }
    }
}