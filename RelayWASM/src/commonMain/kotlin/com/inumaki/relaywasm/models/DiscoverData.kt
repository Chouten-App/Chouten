package com.inumaki.relaywasm.models

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverData @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid = Uuid.random(),
    val url: String,
    val titles: Titles,
    val poster: String,
    val banner: String?,
    val description: String,
    val label: Label,
    val indicator: String?,
    val isWidescreen: Boolean = false,
    val current: Int?,
    val total: Int?
)