package com.inumaki.chouten.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun NetworkImage(url: String, modifier: Modifier) {
    AsyncImage(
        model = url,
        contentScale = ContentScale.Crop,
        contentDescription = null, // Add description if needed,
        modifier = modifier
    )
}