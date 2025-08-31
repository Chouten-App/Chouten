//
// Created by kempc on 17/07/2025.
//

// wasm_core.h
#pragma once

#include <cstdint>
#include "wasm3.h"

#ifdef __cplusplus
extern "C" {
#endif

// Callback types for logging and HTTP
typedef void (*LogCallback)(const char* msg);
typedef const char* (*HttpRequestCallback)(const char* url, int method);

// Struct to pass callbacks into WASM instance
struct WasmEnv {
    LogCallback logger;
    HttpRequestCallback requester;
};

// Layout must match C memory layout for use in WASM
struct HttpResponse {
    uint32_t status_code;
    uint32_t body_ptr;
    uint32_t body_len;
};

// Forward declaration
struct WasmInstance {
    IM3Environment env = nullptr;
    IM3Runtime runtime = nullptr;
    IM3Module module = nullptr;
    uint8_t* wasm_bytes = nullptr;
    WasmEnv callbacks = {};

    ~WasmInstance() {
        if (module) m3_FreeModule(module);
        if (runtime) m3_FreeRuntime(runtime);
        if (env) m3_FreeEnvironment(env);
        if (wasm_bytes) free(wasm_bytes);
    }
};

// Public C interface
WasmInstance* wasm_create(const uint8_t* bytes, uint32_t length);
void wasm_destroy(WasmInstance* instance);
void wasm_set_callbacks(WasmInstance* instance, WasmEnv env);
const char* wasm_call(WasmInstance* instance, const char* name);

#ifdef __cplusplus
}
#endif
