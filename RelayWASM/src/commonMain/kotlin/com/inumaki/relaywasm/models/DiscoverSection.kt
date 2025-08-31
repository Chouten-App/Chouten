package com.inumaki.relaywasm.models

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverSection(
    val title: String,
    val section_type: String, // 0 = Carousel, 1 = list,
    val list: List<DiscoverData>
)