package com.inumaki.chouten.wasm3

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.InputStream

fun InputStream.readAllBytesCompat(): ByteArray {
    val buffer = ByteArrayOutputStream()
    val data = ByteArray(1024)
    var nRead: Int
    while (this.read(data, 0, data.size).also { nRead = it } != -1) {
        buffer.write(data, 0, nRead)
    }
    return buffer.toByteArray()
}

actual class WasmRuntime(private val appContext: Context) {
    init {
        System.loadLibrary("wasm3wrapper")
    }

    private fun loadWasmModuleBytes(): ByteArray {
        return appContext.assets.open("module_test.wasm").use { it.readAllBytesCompat() }
    }

    actual fun loadModule(wasmBytes: ByteArray): WasmModule {
        val wasmModule = WasmModule()

        val bytes = loadWasmModuleBytes()

        wasmModule.setup(loadNativeModule(bytes))

        return wasmModule
    }

    private external fun loadNativeModule(wasmArray: ByteArray?): Long
}

actual class WasmModule {
    private var modulePtr: Long = 0

    fun setup(modulePtr: Long) {
        this.modulePtr = modulePtr
    }

    actual fun callFunction(functionName: String): String {
        return callNativeFunction(modulePtr, functionName)
    }

    private external fun callNativeFunction(modulePtr: Long, functionName: String): String
}

actual suspend fun loadWasmModuleBytes(): ByteArray {

    return ByteArray(0)
}