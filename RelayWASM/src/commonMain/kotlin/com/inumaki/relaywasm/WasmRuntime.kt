package com.inumaki.relaywasm

expect class WasmRuntime {
    fun loadModuleBytes(): ByteArray
    fun loadModule(): WasmModule
    fun dumpMemory(offset: Int, length: Int)
}

expect class WasmModule {
    fun callFunction(functionName: String): String
}