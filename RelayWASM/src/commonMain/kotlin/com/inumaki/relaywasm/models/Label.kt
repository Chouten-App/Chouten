package com.inumaki.relaywasm.models

import kotlinx.serialization.Serializable

@Serializable
data class Label(
    val text: String,
    val color: String
)