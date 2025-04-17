package com.inumaki.chouten.wasm3

actual object WasmRuntime {
    actual fun loadModule(wasmBytes: ByteArray): WasmModule {
        TODO("Not yet implemented")
    }
}

actual object WasmModule {
    actual fun callFunction(functionName: String): String {
        TODO("Not yet implemented")
    }
}

actual suspend fun loadWasmModuleBytes(): ByteArray {
    TODO("Not yet implemented")
}