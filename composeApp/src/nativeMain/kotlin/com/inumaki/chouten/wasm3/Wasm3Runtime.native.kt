package com.inumaki.chouten.wasm3

import kotlinx.cinterop.*
import wasm3.*
import platform.Foundation.*
import platform.posix.memcpy

actual object WasmRuntime {
    @OptIn(ExperimentalForeignApi::class)
    actual fun loadModule(wasmBytes: ByteArray): WasmModule {
        val env = m3_NewEnvironment() ?: error("Failed to create Wasm3 environment")
        val runtime = m3_NewRuntime(env, (64 * 1024).toUInt(), null) ?: error("Failed to create Wasm3 runtime")

        memScoped {
            val modulePointer = alloc<CPointerVar<cnames.structs.M3Module>>()

            val result = m3_ParseModule(
                i_environment = env,
                o_module = modulePointer.ptr,
                i_wasmBytes = wasmBytes.toUByteArray().refTo(0),
                i_numWasmBytes = wasmBytes.size.toUInt()
            )

            if (result != null) {
                val error = result.toKString()
                println("Error parsing module: $error")
                throw IllegalStateException("Failed to parse WASM module: $error")
            }

            val loadResult = m3_LoadModule(runtime, modulePointer.value)
            if (loadResult != null) {
                val error = loadResult.toKString()
                println("Error loading module: $error")
                throw IllegalStateException("Failed to load WASM module: $error")
            }

            return WasmModule
        }
    }
}

actual object WasmModule {
    actual fun callFunction(functionName: String): String {
        TODO("Not yet implemented")
    }
}

actual suspend fun loadWasmModuleBytes(): ByteArray {
    val path = NSBundle.mainBundle.pathForResource("module", "wasm")
        ?: error("module.wasm not found in bundle")
    val data = NSData.dataWithContentsOfFile(path)
        ?: error("Failed to load module.wasm")
    return data.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val buffer = ByteArray(this.length.toInt())
    memScoped {
        memcpy(buffer.refTo(0), this@toByteArray.bytes, this@toByteArray.length)
    }
    return buffer
}