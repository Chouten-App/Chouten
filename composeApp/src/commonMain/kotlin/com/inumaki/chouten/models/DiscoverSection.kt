package com.inumaki.chouten.Models

data class DiscoverSection(
    val title: String,
    val type: Int, // 0 = Carousel, 1 = list,
    val list: List<DiscoverData>
)