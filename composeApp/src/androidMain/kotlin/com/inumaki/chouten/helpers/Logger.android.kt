package com.inumaki.chouten.helpers

import android.util.Log

actual object PlatformLogger : Logger {
    override fun log(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}