#include <jni.h>
#include "../../nativeInterop/cinterop/wasm3/source/wasm3.h"

#define WASM_STACK_SLOTS    1024
#define NATIVE_STACK_SIZE   (32*1024)

struct WasmInstance {
    IM3Environment env = nullptr;
    IM3Runtime runtime = nullptr;
    IM3Module module = nullptr;
    uint8_t* wasm_bytes = nullptr;

    ~WasmInstance() {
        if (module) m3_FreeModule(module);
        if (runtime) m3_FreeRuntime(runtime);
        if (env) m3_FreeEnvironment(env);
        if (wasm_bytes) free(wasm_bytes);
    }
};

extern "C"
JNIEXPORT jlong JNICALL
Java_com_inumaki_chouten_wasm3_WasmRuntime_loadNativeModule(JNIEnv *env, jobject thiz, jbyteArray wasmArray) {
    jsize jlength = env->GetArrayLength(wasmArray);
    if (jlength <= 0) return 0;

    uint32_t length = static_cast<uint32_t>(jlength);
    uint8_t* wasm_bytes = static_cast<uint8_t*>(malloc(length));
    if (!wasm_bytes) return 0;

    env->GetByteArrayRegion(wasmArray, 0, length, reinterpret_cast<jbyte*>(wasm_bytes));

    auto* instance = new WasmInstance();
    instance->wasm_bytes = wasm_bytes;
    instance->env = m3_NewEnvironment();
    if (!instance->env) {
        delete instance;
        return 0;
    }

    instance->runtime = m3_NewRuntime(instance->env, WASM_STACK_SLOTS, nullptr);
    if (!instance->runtime) {
        delete instance;
        return 0;
    }

    M3Result result = m3_ParseModule(instance->env, &instance->module, wasm_bytes, length);
    if (result != m3Err_none) {
        delete instance;
        return 0;
    }

    result = m3_LoadModule(instance->runtime, instance->module);
    if (result != m3Err_none) {
        delete instance;
        return 0;
    }

    jlong returnValue = reinterpret_cast<jlong>(instance);

    return returnValue;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_inumaki_chouten_wasm3_WasmModule_callNativeFunction(JNIEnv* env, jobject thiz, jlong ptr, jstring jname) {
    // Retrieve the WasmInstance
    WasmInstance* instance = reinterpret_cast<WasmInstance*>(ptr);
    if (!instance) {
        return nullptr; // Ensure instance is not null
    }

    const char* name = env->GetStringUTFChars(jname, nullptr);
    if (name == nullptr) {
        return nullptr; // Ensure name is valid
    }

    // Lookup function
    IM3Function func;
    M3Result result = m3_FindFunction(&func, instance->runtime, name);
    env->ReleaseStringUTFChars(jname, name);

    if (result != m3Err_none) {
        return nullptr; // If function lookup failed, return null
    }

    // Prepare and call (no args)
    result = m3_CallArgv(func, 0, nullptr);
    if (result != m3Err_none) {
        return nullptr; // If function call failed, return null
    }

    // Assume the function returns a pointer to a null-terminated UTF-8 string
    uint32_t str_offset;
    const void* retptrs[1] = { &str_offset };
    result = m3_GetResults(func, 1, retptrs);
    if (result != m3Err_none) {
        return nullptr; // If results retrieval failed, return null
    }

    // Get access to WASM memory buffer
    uint8_t* memory = m3_GetMemory(instance->runtime, nullptr, 0);
    if (!memory) {
        return nullptr; // If memory retrieval failed, return null
    }

    // Locate the string in memory (ensure null-terminated)
    const char* wasm_str = reinterpret_cast<const char*>(memory + str_offset);
    if (!wasm_str) {
        return nullptr; // If the string is null, return null
    }

    // Return as jstring
    return env->NewStringUTF(wasm_str);
}

