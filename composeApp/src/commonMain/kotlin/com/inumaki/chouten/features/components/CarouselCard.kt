package com.inumaki.chouten.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.chouten.Models.DiscoverData
import com.inumaki.chouten.components.NetworkImage

@Composable
fun CarouselCard(data: DiscoverData) {
    Box(
        modifier = Modifier
            .height(440.dp)
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, Color(0xFF3B3B3B), shape = RoundedCornerShape(20.dp))
            .background(Color(0xFF171717))
            .clickable {
                println("Clicked on ${data.url}")

                /*
                val encodedUrl = URLEncoder.encode(data.url, "UTF-8")
                // open info view with data.url
                navController.navigate(
                    "InfoView/${encodedUrl}"
                )*/
            }
    ) {
        NetworkImage(
            url = data.poster,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x00171717),
                            Color(0xFF171717)
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                data.titles.secondary ?: "",
                color = Color(0xB2D4D4D4),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    data.titles.primary,
                    color = Color(0xFFD4D4D4),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Icon(
                    modifier = Modifier.width(16.dp),
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "",
                    tint = Color(0xFFBB3834)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                data.description,
                color = Color(0xB2D4D4D4),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}