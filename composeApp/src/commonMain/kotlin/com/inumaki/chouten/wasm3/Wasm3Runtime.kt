package com.inumaki.chouten.wasm3

expect suspend fun loadWasmModuleBytes(): ByteArray

expect class WasmRuntime {
    fun loadModule(wasmBytes: ByteArray): WasmModule
}

expect class WasmModule {
    fun callFunction(functionName: String): String
}