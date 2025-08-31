package com.inumaki.relaywasm

open class Log(
    val tag: String,
    val message: String
)

object LogManager {
    val logs: MutableList<Log> = mutableListOf()

    fun log(tag: String, message: String) {
        println("$tag: $message")
        logs.add(Log(tag, message))
    }
}