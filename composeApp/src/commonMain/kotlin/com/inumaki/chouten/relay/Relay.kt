package com.inumaki.chouten.relay

import com.inumaki.chouten.Models.DiscoverSection

expect object Relay {
    val version: String

    fun init()

    suspend fun discover(): List<DiscoverSection>
}