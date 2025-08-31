package com.inumaki.relaywasm

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths

@Suppress("UnsafeDynamicallyLoadedCode")
actual class WasmRuntime {
    private var instancePtr: Long = 0


    init {
        // Adjust path to your DLL file as needed
        // You can use System.loadLibrary("RelayWASM") if DLL is in java.library.path
        // Or System.load("absolute_path_to_dll") for explicit path
        System.loadLibrary("RelayWASM")
    }

    private fun loadWasmModuleBytes(): ByteArray {
        // Read WASM file from a known desktop path or resource
        val path = Paths.get("C://Users/kempc/development/rust/module_test/target/wasm32-unknown-unknown/release/module_test.wasm")
        return Files.readAllBytes(path)
    }

    actual fun loadModuleBytes(): ByteArray {
        val path = Paths.get("C://Users/kempc/development/rust/module_test/target/wasm32-unknown-unknown/release/module_test.wasm")
        return Files.readAllBytes(path)
    }

    actual fun loadModule(): WasmModule {
        val wasmModule = WasmModule()

        val bytes = loadWasmModuleBytes()

        instancePtr = loadNativeModule(bytes)

        wasmModule.setup(instancePtr)

        return wasmModule
    }

    actual fun dumpMemory(offset: Int, length: Int) {
        val buffer: ByteBuffer = getMemory(instancePtr)
        val limit = (offset + length).coerceAtMost(buffer.capacity())

        // Copy the relevant slice into a ByteArray
        val byteArray = ByteArray(limit - offset)
        for (i in offset until limit) {
            byteArray[i - offset] = buffer.get(i)
        }

        // Write to a binary file
        File("memory.bin").writeBytes(byteArray)
    }

    private external fun loadNativeModule(wasmArray: ByteArray?): Long

    private external fun getMemory(ptr: Long): ByteBuffer
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
