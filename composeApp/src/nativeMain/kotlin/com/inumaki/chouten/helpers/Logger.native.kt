package com.inumaki.chouten.helpers

import co.touchlab.kermit.Severity
import platform.Foundation.NSLog

actual object PlatformLogger : Logger {
    override fun log(tag: String, message: String) {
        co.touchlab.kermit.Logger.log(
            Severity.Info, tag, message = message,
            throwable = null
        )
        // NSLog("$tag: $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        NSLog("$tag [ERROR]: $message ${""}")
    }
}