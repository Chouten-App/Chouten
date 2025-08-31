//
// Created by kempc on 17/07/2025.
//

// wasm_core.cpp
#include "wasm_core.h"
#include <cstring>
#include <cstdlib>
#include <string>
#include <cstring>
#include "../wasm3/source/wasm3.h"

static const void* log_host(IM3Runtime runtime, IM3ImportContext, uint64_t* _sp, void*) {
    auto* instance = static_cast<WasmInstance*>(m3_GetUserData(runtime));
    if (!instance || !instance->callbacks.logger) return nullptr;

    auto* memory = m3_GetMemory(runtime, nullptr, 0);
    auto ptr = static_cast<uint32_t>(_sp[0]);
    auto len = static_cast<uint32_t>(_sp[1]);

    std::string msg(reinterpret_cast<const char*>(memory + ptr), len);
    instance->callbacks.logger(msg.c_str());
    return nullptr;
}

static const void* request_host(IM3Runtime runtime, IM3ImportContext, uint64_t* _sp, void*) {
    auto* instance = static_cast<WasmInstance*>(m3_GetUserData(runtime));
    if (!instance || !instance->callbacks.requester) return nullptr;

    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    auto urlPtr = static_cast<uint32_t>(_sp[1]);
    auto urlLen = static_cast<uint32_t>(_sp[2]);
    auto method = static_cast<uint32_t>(_sp[3]);

    std::string url(reinterpret_cast<const char*>(memory + urlPtr), urlLen);
    const char* response = instance->callbacks.requester(url.c_str(), method);
    if (!response) return nullptr;

    std::string body(response);
    free((void*)response);

    IM3Function allocFunc = nullptr;
    if (m3_FindFunction(&allocFunc, runtime, "alloc") != m3Err_none) return nullptr;

    auto bodySize = static_cast<uint32_t>(body.size());
    const void* args[1] = { &bodySize };
    if (m3_Call(allocFunc, 1, args) != m3Err_none) return nullptr;

    uint32_t bodyPtr = 0;
    const void* rets[1] = { &bodyPtr };
    if (m3_GetResults(allocFunc, 1, rets) != m3Err_none) return nullptr;
    std::memcpy(memory + bodyPtr, body.data(), bodySize);

    uint32_t structSize = sizeof(HttpResponse);
    args[0] = &structSize;
    if (m3_Call(allocFunc, 1, args) != m3Err_none) return nullptr;

    uint32_t structPtr = 0;
    rets[0] = &structPtr;
    if (m3_GetResults(allocFunc, 1, rets) != m3Err_none) return nullptr;

    HttpResponse resp { 200, bodyPtr, bodySize };
    std::memcpy(memory + structPtr, &resp, sizeof(HttpResponse));
    return nullptr;
}

WasmInstance* wasm_create(const uint8_t* bytes, uint32_t length) {
    auto* instance = new WasmInstance();
    instance->wasm_bytes = static_cast<uint8_t*>(malloc(length));
    std::memcpy(instance->wasm_bytes, bytes, length);

    instance->env = m3_NewEnvironment();
    instance->runtime = m3_NewRuntime(instance->env, 1024, instance);

    if (m3_ParseModule(instance->env, &instance->module, instance->wasm_bytes, length) != m3Err_none ||
        m3_LoadModule(instance->runtime, instance->module) != m3Err_none) {
        delete instance;
        return nullptr;
    }

    m3_LinkRawFunction(instance->module, "env", "log_host", "v(ii)", log_host);
    m3_LinkRawFunction(instance->module, "env", "request_host", "i(iii)", request_host);

    return instance;
}

void wasm_destroy(WasmInstance* instance) {
    delete instance;
}

void wasm_set_callbacks(WasmInstance* instance, WasmEnv env) {
    if (instance) {
        instance->callbacks = env;
    }
}

const char* wasm_call(WasmInstance* instance, const char* name) {
    if (!instance || !name) return nullptr;

    IM3Function func = nullptr;
    if (m3_FindFunction(&func, instance->runtime, name) != m3Err_none) return nullptr;

    if (m3_CallArgv(func, 0, nullptr) != m3Err_none) return nullptr;

    uint32_t offset;
    const void* ret[1] = { &offset };
    if (m3_GetResults(func, 1, ret) != m3Err_none) return nullptr;

    const char* str = reinterpret_cast<const char*>(m3_GetMemory(instance->runtime, nullptr, 0) + offset);
    return strdup(str); // caller must free
}
