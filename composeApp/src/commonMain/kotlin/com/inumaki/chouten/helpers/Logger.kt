package com.inumaki.chouten.helpers

interface Logger {
    fun log(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

expect object PlatformLogger : Logger