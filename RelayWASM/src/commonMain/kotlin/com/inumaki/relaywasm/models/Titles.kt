package com.inumaki.relaywasm.models

import kotlinx.serialization.Serializable

@Serializable
data class Titles(
    val primary: String,
    val secondary: String?,
)