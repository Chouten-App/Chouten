package com.inumaki.relaywasm.models

import kotlinx.serialization.Serializable

@Serializable
sealed class ChoutenError {
    @Serializable
    data class Network(val url: String, val message: String) : ChoutenError()

    @Serializable
    data class HtmlParse(val selector: String, val message: String) : ChoutenError()

    @Serializable
    data class Host(val function: String, val message: String) : ChoutenError()

    @Serializable
    data class Module(val message: String) : ChoutenError()
}