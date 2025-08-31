package com.inumaki.relaywasm

import android.util.Log

actual fun platform() = "Android"

actual class RelayWASM {
    actual fun init() {
        Log.d("WASM", "Initializing RelayWASM.")
        // Setup WASM3

        // Setup WASM/Kotlin Bridging

        Log.d("WASM", "Initialized RelayWASM.")
    }
}