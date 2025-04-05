package com.inumaki.chouten

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform