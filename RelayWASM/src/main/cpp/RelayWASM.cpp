//
//
// RelayWASM/WASM3 Bridging file
// Main function connections should be added to this file.
//

#include <jni.h>
#include "wasm3/source/wasm3.h"
#include <string>
#include <cstring>

#define JNI_VERSION JNI_VERSION_1_6

#define WASM_STACK_SLOTS    1024*1024*5

JavaVM* g_vm = nullptr;
jclass g_jniBridgeClass = nullptr;
jmethodID g_onWasmLogMethod = nullptr;
jmethodID g_onWasmRequestMethod = nullptr;
jmethodID g_onWasmHtmlParseMethod = nullptr;
jmethodID g_onWasmHtmlQuerySelectorMethod = nullptr;
jmethodID g_onWasmHtmlQuerySelectorAllMethod = nullptr;
jmethodID g_onWasmNodeTextMethod = nullptr;
jmethodID g_onWasmNodeAttrMethod = nullptr;
jmethodID g_onWasmNodeQuerySelectorMethod = nullptr;
jmethodID g_onWasmNodeQuerySelectorAllMethod = nullptr;
jmethodID g_onWasmGetSettingInGroupMethod = nullptr;

struct HttpResponse {
    uint32_t status_code;
    uint32_t body_ptr;
    uint32_t body_len;
};

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void*) {
    g_vm = vm;
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION) != JNI_OK) {
        return JNI_ERR;
    }

    jclass localRef = env->FindClass("com/inumaki/relaywasm/JNIBridge");
    g_jniBridgeClass = reinterpret_cast<jclass>(env->NewGlobalRef(localRef));
    env->DeleteLocalRef(localRef);

    // Host functions
    g_onWasmLogMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmLog", "(Ljava/lang/String;)V");
    g_onWasmRequestMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmRequest", "(Ljava/lang/String;I)Ljava/lang/String;");

    g_onWasmHtmlParseMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmHtmlParse", "(Ljava/lang/String;)I");

    g_onWasmHtmlQuerySelectorMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmHtmlQuerySelector", "(Ljava/lang/String;I)I");
    g_onWasmHtmlQuerySelectorAllMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmHtmlQuerySelectorAll", "(Ljava/lang/String;I)[I");

    g_onWasmNodeTextMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmNodeText", "(I)Ljava/lang/String;");
    g_onWasmNodeAttrMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmNodeAttr", "(ILjava/lang/String;)Ljava/lang/String;");
    g_onWasmNodeQuerySelectorMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmNodeQuerySelector", "(Ljava/lang/String;I)I");
    g_onWasmNodeQuerySelectorAllMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmNodeQuerySelectorAll", "(Ljava/lang/String;I)[I");
    g_onWasmGetSettingInGroupMethod = env->GetStaticMethodID(g_jniBridgeClass, "onWasmGetSettingInGroup", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
    return JNI_VERSION;
}

void LogToKotlin(const char* msg) {
    if (!g_vm || !g_jniBridgeClass || !g_onWasmLogMethod) {
        // Fallback: print to stderr if JNI not ready
        fprintf(stderr, "LogToKotlin (no JNI): %s\n", msg);
        return;
    }

    JNIEnv* env = nullptr;
    bool attached = false;
    jint getEnvStat = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (getEnvStat == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
            if (g_vm->AttachCurrentThread(reinterpret_cast<void **>(&env), nullptr) != JNI_OK) {
#endif
            fprintf(stderr, "LogToKotlin failed to attach thread\n");
            return;
        }
        attached = true;
    } else if (getEnvStat != JNI_OK) {
        fprintf(stderr, "LogToKotlin failed to get env\n");
        return;
    }

    jstring jMsg = env->NewStringUTF(msg);
    if (jMsg) {
        env->CallStaticVoidMethod(g_jniBridgeClass, g_onWasmLogMethod, jMsg);
        env->DeleteLocalRef(jMsg);
    }

    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
    }

    if (attached) {
        g_vm->DetachCurrentThread();
    }
}

#ifdef __ANDROID__
#include <android/log.h>
#define LOG_TAG "Wasm3Thunk"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#else
#include <cstdio>

#define LOGI(fmt, ...) do { \
    char buf[1024]; \
    snprintf(buf, sizeof(buf), fmt, ##__VA_ARGS__); \
    LogToKotlin(buf); \
} while(0)

#define LOGE(fmt, ...) do { \
    char buf[1024]; \
    snprintf(buf, sizeof(buf), fmt, ##__VA_ARGS__); \
    LogToKotlin(buf); \
} while(0)
#endif

const void* request_host(IM3Runtime runtime, IM3ImportContext _ctx, uint64_t* _sp, void* _mem) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    if (!memory) {
        LOGE("No WASM memory");
        return nullptr;
    }

    uint32_t memSize = m3_GetMemorySize(runtime);
     
    uint32_t urlPtr = static_cast<uint32_t>(_sp[1]);
    uint32_t urlLen = static_cast<uint32_t>(_sp[2]);
    uint32_t method = static_cast<uint32_t>(_sp[3]);

    if (urlPtr >= memSize || urlPtr + urlLen > memSize) {
        LOGE("Invalid URL pointer or length");
        return nullptr;
    }

    std::string url(reinterpret_cast<const char*>(memory + urlPtr), urlLen);

    if (!g_vm || !g_jniBridgeClass || !g_onWasmRequestMethod) {
        LOGE("JNI environment not ready");
        return nullptr;
    }

    JNIEnv* env = nullptr;
    bool attached = false;

    jint getEnvStat = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (getEnvStat == JNI_EDETACHED) {
        #ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
        #else
        if (g_vm->AttachCurrentThread(reinterpret_cast<void **>(&env), nullptr) != JNI_OK) {
        #endif
            LOGE("Failed to attach current thread");
            return nullptr;
        }
        attached = true;
    } else if (getEnvStat != JNI_OK) {
        LOGE("Failed to get JNI env");
        return nullptr;
    }

    // Create Java String for URL
    jstring jUrl = env->NewStringUTF(url.c_str());
    if (!jUrl) {
        LOGE("Failed to create Java string for URL");
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    // Call Kotlin method
    auto jResult = (jstring) env->CallStaticObjectMethod(g_jniBridgeClass, g_onWasmRequestMethod, jUrl, (jint)method);

    env->DeleteLocalRef(jUrl);

    // Check for exceptions
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    if (!jResult) {
        LOGE("Kotlin onWasmRequest returned null");
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    // Convert returned Java string to C string
    const char* resultCStr = env->GetStringUTFChars(jResult, nullptr);
    if (!resultCStr) {
        LOGE("Failed to get UTF chars from returned Java string");
        env->DeleteLocalRef(jResult);
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    jint statusCode = 200;
    std::string resultStr(resultCStr);

    env->ReleaseStringUTFChars(jResult, resultCStr);
    env->DeleteLocalRef(jResult);

    // 1️⃣ Get WASM alloc function
    IM3Function allocFunc = nullptr;
    M3Result res = m3_FindFunction(&allocFunc, runtime, "alloc");
    if (res != m3Err_none) {
        LOGE("Failed to find alloc function: %s", res);
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    auto bodySize = static_cast<uint32_t>(resultStr.size());

    uint32_t requiredPages = (bodySize + 65535) / 65536; // round up pages needed
    IM3Function memoryGrowFunc = nullptr;
    M3Result result = m3_FindFunction(&memoryGrowFunc, runtime, "grow_memory");
    if (result != m3Err_none) {
        LOGE("grow_memory function not found");
        // handle error
    }

// Grow memory by N pages (1 page = 64KiB)
    uint32_t currentPages = m3_GetMemorySize(runtime) / 65536;

    if (requiredPages > 0) {
        uint32_t pagesToGrow = requiredPages;
        const void* growArg[1] = { &pagesToGrow };
        result = m3_Call(memoryGrowFunc, 1, growArg);
        if (result != m3Err_none) {
            LOGE("grow_memory call failed");
            // handle error
        }

    // Get the result - previous memory size in pages or -1 on failure
        uint32_t prevSize = 0;
        const void* growResult[1] = { &prevSize };
        result = m3_GetResults(memoryGrowFunc, 1, growResult);
        if (result != m3Err_none) {
            LOGE("grow_memory get results failed");
            // handle error
        }

        if (prevSize == (uint32_t)-1) {
            LOGE("memory grow failed");
        }
    }

    memory = m3_GetMemory(runtime, nullptr, 0);
    memSize = m3_GetMemorySize(runtime);

    // 2️⃣ === Allocate space for body string ===
    const void* strArgs[1] = { &bodySize };
    res = m3_Call(allocFunc, 1, strArgs);
    if (res != m3Err_none) {
        LOGE("alloc call for body failed: %s", res);
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    // 3️⃣ Get body allocation pointer
    uint32_t bodyPtr = 0;
    const void* strRet[1] = { &bodyPtr };
    res = m3_GetResults(allocFunc, 1, strRet);
    if (res != m3Err_none) {
        LOGE("alloc get results for body failed: %s", res);
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    // 4️⃣ Copy body string into WASM memory
    memory = m3_GetMemory(runtime, nullptr, 0);
    memcpy(memory + bodyPtr, resultStr.data(), bodySize);

    // 5️⃣ === Allocate space for HttpResponse struct ===
    uint32_t structSize = sizeof(HttpResponse);


    const void* structArgs[1] = { &structSize };
    res = m3_Call(allocFunc, 1, structArgs);
    if (res != m3Err_none) {
        LOGE("alloc call for struct failed: %s", res);
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    // 6️⃣ Get struct allocation pointer
    uint32_t structPtr = 0;
    const void* structRet[1] = { &structPtr };
    res = m3_GetResults(allocFunc, 1, structRet);
    if (res != m3Err_none) {
        LOGE("alloc get results for struct failed: %s", res);
        if (attached) g_vm->DetachCurrentThread();
        return nullptr;
    }

    // 7️⃣ Write HttpResponse struct fields into WASM memory
    // Caution: Must match the struct layout exactly!
    HttpResponse resp = {
            .status_code = 200,
            .body_ptr = bodyPtr,
            .body_len = bodySize
    };

    memory = m3_GetMemory(runtime, nullptr, 0);
    memcpy(memory + structPtr, &resp, structSize);

// 8️⃣ Detach JNI if needed
    if (attached) {
        g_vm->DetachCurrentThread();
    }
    return nullptr;
}

const void* log_host(IM3Runtime runtime, IM3ImportContext _ctx, uint64_t * _sp, void * _mem) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    if (memory) {
        auto stringPtr = static_cast<uint32_t>(_sp[0]);
        auto stringLen = static_cast<uint32_t>(_sp[1]);

        uint32_t memSize = m3_GetMemorySize(runtime);
        if (stringPtr < memSize && stringPtr + stringLen <= memSize && stringLen > 0) {
            const char* str = reinterpret_cast<const char*>(memory + stringPtr);
            std::string msg(str, stringLen);  // always safe

            // Never pass nullptr to LOGI
            if (!msg.empty()) {
                if (g_vm && g_onWasmLogMethod) {
                    JNIEnv* env = nullptr;
                    bool attached = false;

                    jint getEnvStat = g_vm->GetEnv((void**)&env, JNI_VERSION);
                    if (getEnvStat == JNI_EDETACHED) {
#ifdef __ANDROID__
                        if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
                            if (g_vm->AttachCurrentThread(reinterpret_cast<void **>(&env), nullptr) != JNI_OK) {
#endif
                            attached = true;
                        }
                    }

                    if (env) {
                        jstring jmsg = env->NewStringUTF(msg.c_str());
                        env->CallStaticVoidMethod(g_jniBridgeClass, g_onWasmLogMethod, jmsg);
                        env->DeleteLocalRef(jmsg);
                    }

                    if (attached) {
                        g_vm->DetachCurrentThread();
                    }
                }

            } else {
                LOGI("WASM_LOG: <empty>");
            }
        } else {
            LOGI("WASM_LOG: <invalid pointer or length>");
        }
    } else {
        LOGI("WASM_LOG: <no memory>");
    }
    return nullptr;
}

const void* html_parse_host(IM3Runtime runtime, IM3ImportContext _ctx, uint64_t * _sp, void * _mem) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);

    if (!memory) {
        LOGI("HTML_PARSE: <no memory>");
        return nullptr;
    }

    auto stringPtr = static_cast<uint32_t>(_sp[1]);
    auto stringLen = static_cast<uint32_t>(_sp[2]);

    uint32_t memSize = m3_GetMemorySize(runtime);

    if (stringPtr >= memSize || stringPtr + stringLen > memSize || stringLen <= 0) {
        LOGI("HTML_PARSE: <invalid pointer or length>");
        return nullptr;
    }

    const char* str = reinterpret_cast<const char*>(memory + stringPtr);
    std::string html(str, stringLen);

    if (html.empty()) {
        LOGI("HTML_PARSE: <empty>");
        return nullptr;
    }

    if (!g_vm || !g_jniBridgeClass || !g_onWasmHtmlParseMethod) {
        LOGE("JNI environment not ready");
        return nullptr;
    }

    JNIEnv* env = nullptr;
    bool attached = false;

    jint getEnvStat = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (getEnvStat == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
            if (g_vm->AttachCurrentThread(reinterpret_cast<void **>(&env), nullptr) != JNI_OK) {
#endif
            attached = true;
        }
    }

    if (env) {
        jstring jmsg = env->NewStringUTF(html.c_str());
        uint32_t doc_id = env->CallStaticIntMethod(g_jniBridgeClass, g_onWasmHtmlParseMethod, jmsg);
        env->DeleteLocalRef(jmsg);

        _sp[0] = doc_id;
    }


    if (attached) {
        g_vm->DetachCurrentThread();
    }

    return nullptr;
}

const void* html_query_selector_host(IM3Runtime runtime, IM3ImportContext _ctx, uint64_t * _sp, void * _mem) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);

    if (!memory) {
        LOGI("HTML_PARSE: <no memory>");
        return nullptr;
    }

    auto docId = static_cast<uint32_t>(_sp[1]);
    auto stringPtr = static_cast<uint32_t>(_sp[2]);
    auto stringLen = static_cast<uint32_t>(_sp[3]);

    uint32_t memSize = m3_GetMemorySize(runtime);

    if (stringPtr >= memSize || stringPtr + stringLen > memSize || stringLen <= 0) {
        LOGI("HTML_PARSE: <invalid pointer or length>");
        return nullptr;
    }

    const char* str = reinterpret_cast<const char*>(memory + stringPtr);
    std::string query(str, stringLen);

    if (query.empty()) {
        LOGI("HTML_PARSE: <empty>");
        return nullptr;
    }

    if (g_vm == nullptr || g_onWasmHtmlQuerySelectorMethod == nullptr) return nullptr;

    JNIEnv* env = nullptr;
    bool attached = false;

    jint getEnvStat = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (getEnvStat == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
            if (g_vm->AttachCurrentThread(reinterpret_cast<void **>(&env), nullptr) != JNI_OK) {
#endif
            attached = true;
        }
    }

    if (env) {
        jstring jmsg = env->NewStringUTF(query.c_str());
        jint jdoc_id = static_cast<jint>(docId);
        uint32_t element_id = env->CallStaticIntMethod(g_jniBridgeClass, g_onWasmHtmlQuerySelectorMethod, jmsg, jdoc_id);
        env->DeleteLocalRef(jmsg);

        _sp[0] = element_id;
    }


    if (attached) {
        g_vm->DetachCurrentThread();
    }

    return nullptr;
}

const void* html_query_selector_all_host(IM3Runtime runtime, IM3ImportContext _ctx, uint64_t * _sp, void * _mem) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);

    if (!memory) {
        LOGI("HTML_PARSE: <no memory>");
        return nullptr;
    }

    auto docId = static_cast<uint32_t>(_sp[1]);
    auto stringPtr = static_cast<uint32_t>(_sp[2]);
    auto stringLen = static_cast<uint32_t>(_sp[3]);
    auto outPtr = static_cast<uint32_t>(_sp[4]);

    uint32_t memSize = m3_GetMemorySize(runtime);

    if (stringPtr >= memSize || stringPtr + stringLen > memSize || stringLen <= 0) {
        LOGI("HTML_PARSE: <invalid pointer or length>");
        return nullptr;
    }

    const char* str = reinterpret_cast<const char*>(memory + stringPtr);
    std::string query(str, stringLen);

    if (query.empty()) {
        LOGI("HTML_PARSE: <empty>");
        return nullptr;
    }

    if (g_vm == nullptr || g_onWasmHtmlQuerySelectorMethod == nullptr) return nullptr;

    JNIEnv* env = nullptr;
    bool attached = false;

    jint getEnvStat = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (getEnvStat == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
            if (g_vm->AttachCurrentThread(reinterpret_cast<void **>(&env), nullptr) != JNI_OK) {
#endif
            attached = true;
        }
    }

    if (env) {
        jstring jQuery = env->NewStringUTF(query.c_str());
        jint jDocId = static_cast<jint>(docId);

        // Call Kotlin method returning int[]
        jintArray jResultArray = (jintArray) env->CallStaticObjectMethod(
            g_jniBridgeClass, 
            g_onWasmHtmlQuerySelectorAllMethod, 
            jQuery, 
            jDocId
        );
        env->DeleteLocalRef(jQuery);

        if (jResultArray != nullptr) {
            jsize length = env->GetArrayLength(jResultArray);
            jint* elements = env->GetIntArrayElements(jResultArray, nullptr);

            // Allocate WASM memory
            IM3Function allocFunc = nullptr;
            m3_FindFunction(&allocFunc, runtime, "alloc");
            uint32_t wasmPtr = 0;
            if (allocFunc) {
                uint32_t allocSize = length * sizeof(uint32_t);
                const void* allocArgs[1] = { &allocSize };
                m3_Call(allocFunc, 1, allocArgs);

                const void* allocResults[1] = { &wasmPtr };
                m3_GetResults(allocFunc, 1, allocResults);
                wasmPtr = allocResults[0] ? *(uint32_t*)allocResults[0] : 0;
            }

            // Copy array directly with memcpy
            uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
            if (wasmPtr != 0 && memory) {
                memcpy(memory + wasmPtr, elements, length * sizeof(uint32_t));
            }

            // Release JNI array
            env->ReleaseIntArrayElements(jResultArray, elements, 0);
            env->DeleteLocalRef(jResultArray);

            // Write pointer and length back to WASM
            *(uint32_t*)(memory + outPtr) = wasmPtr;

            _sp[0] = length;
        }
    }


    if (attached) {
        g_vm->DetachCurrentThread();
    }

    return nullptr;
}


const void* html_node_query_selector_host(IM3Runtime runtime, IM3ImportContext _ctx, uint64_t * _sp, void * _mem) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);

    if (!memory) {
        LOGI("HTML_PARSE: <no memory>");
        return nullptr;
    }

    auto elementId = static_cast<uint32_t>(_sp[1]);
    auto stringPtr = static_cast<uint32_t>(_sp[2]);
    auto stringLen = static_cast<uint32_t>(_sp[3]);

    uint32_t memSize = m3_GetMemorySize(runtime);

    if (stringPtr >= memSize || stringPtr + stringLen > memSize || stringLen <= 0) {
        LOGI("HTML_PARSE: <invalid pointer or length>");
        return nullptr;
    }

    const char* str = reinterpret_cast<const char*>(memory + stringPtr);
    std::string query(str, stringLen);

    if (query.empty()) {
        LOGI("HTML_PARSE: <empty>");
        return nullptr;
    }

    if (g_vm == nullptr || g_onWasmHtmlQuerySelectorMethod == nullptr) return nullptr;

    JNIEnv* env = nullptr;
    bool attached = false;

    jint getEnvStat = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (getEnvStat == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
            if (g_vm->AttachCurrentThread(reinterpret_cast<void **>(&env), nullptr) != JNI_OK) {
#endif
            attached = true;
        }
    }

    if (env) {
        jstring jmsg = env->NewStringUTF(query.c_str());
        jint jelementId = static_cast<jint>(elementId);
        uint32_t element_id = env->CallStaticIntMethod(g_jniBridgeClass, g_onWasmNodeQuerySelectorMethod, jmsg, jelementId);
        env->DeleteLocalRef(jmsg);

        _sp[0] = element_id;
    }


    if (attached) {
        g_vm->DetachCurrentThread();
    }

    return nullptr;
}

const void* html_node_query_selector_all_host(IM3Runtime runtime, IM3ImportContext _ctx, uint64_t * _sp, void * _mem) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);

    if (!memory) {
        LOGI("HTML_PARSE: <no memory>");
        return nullptr;
    }

    auto elementId = static_cast<uint32_t>(_sp[1]);
    auto stringPtr = static_cast<uint32_t>(_sp[2]);
    auto stringLen = static_cast<uint32_t>(_sp[3]);
    auto outPtr = static_cast<uint32_t>(_sp[4]);

    uint32_t memSize = m3_GetMemorySize(runtime);

    if (stringPtr >= memSize || stringPtr + stringLen > memSize || stringLen <= 0) {
        LOGI("HTML_PARSE: <invalid pointer or length>");
        return nullptr;
    }

    const char* str = reinterpret_cast<const char*>(memory + stringPtr);
    std::string query(str, stringLen);

    if (query.empty()) {
        LOGI("HTML_PARSE: <empty>");
        return nullptr;
    }

    if (g_vm == nullptr || g_onWasmHtmlQuerySelectorMethod == nullptr) return nullptr;

    JNIEnv* env = nullptr;
    bool attached = false;

    jint getEnvStat = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (getEnvStat == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
            if (g_vm->AttachCurrentThread(reinterpret_cast<void **>(&env), nullptr) != JNI_OK) {
#endif
            attached = true;
        }
    }

    if (env) {
        jstring jQuery = env->NewStringUTF(query.c_str());
        jint jelementId = static_cast<jint>(elementId);

        // Call Kotlin method returning int[]
        jintArray jResultArray = (jintArray) env->CallStaticObjectMethod(
            g_jniBridgeClass, 
            g_onWasmNodeQuerySelectorAllMethod, 
            jQuery, 
            jelementId
        );
        env->DeleteLocalRef(jQuery);

        if (jResultArray != nullptr) {
            jsize length = env->GetArrayLength(jResultArray);
            jint* elements = env->GetIntArrayElements(jResultArray, nullptr);

            // Allocate WASM memory
            IM3Function allocFunc = nullptr;
            m3_FindFunction(&allocFunc, runtime, "alloc");
            uint32_t wasmPtr = 0;
            if (allocFunc) {
                uint32_t allocSize = length * sizeof(uint32_t);
                const void* allocArgs[1] = { &allocSize };
                m3_Call(allocFunc, 1, allocArgs);

                const void* allocResults[1] = { &wasmPtr };
                m3_GetResults(allocFunc, 1, allocResults);
                wasmPtr = allocResults[0] ? *(uint32_t*)allocResults[0] : 0;
            }

            // Copy array directly with memcpy
            uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
            if (wasmPtr != 0 && memory) {
                memcpy(memory + wasmPtr, elements, length * sizeof(uint32_t));
            }

            // Release JNI array
            env->ReleaseIntArrayElements(jResultArray, elements, 0);
            env->DeleteLocalRef(jResultArray);

            // Write pointer and length back to WASM
            *(uint32_t*)(memory + outPtr) = wasmPtr;

            _sp[0] = length;
        }
    }


    if (attached) {
        g_vm->DetachCurrentThread();
    }

    return nullptr;
}

// env import: "i(ii)"  -> returns i32 (length), args: (i32 node_id, i32 out_ptr_addr)
const void* html_node_text_host(IM3Runtime runtime, IM3ImportContext, uint64_t* sp, void*) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    if (!memory) {
        // Return 0 length
        sp[0] = 0;
        return nullptr;
    }

    const uint32_t node_id     = (uint32_t) sp[1]; // arg0
    const uint32_t out_ptr_addr = (uint32_t) sp[2]; // arg1: address (in WASM mem) where we must write the pointer (u32)

    if (!g_vm || !g_onWasmNodeTextMethod) {
        sp[0] = 0;
        return nullptr;
    }

    JNIEnv* env = nullptr;
    bool attached = false;

    jint s = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (s == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) == JNI_OK) {
            attached = true;
        } else {
            sp[0] = 0;
            return nullptr;
        }
#else
        if (g_vm->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) == JNI_OK) {
            attached = true;
        } else {
            sp[0] = 0;
            return nullptr;
        }
#endif
    }

    uint32_t len = 0;
    uint32_t strPtr = 0;

    if (env) {
        // Call Kotlin: JNIBridge.onWasmNodeText(nodeId): String
        jstring jText = (jstring) env->CallStaticObjectMethod(g_jniBridgeClass, g_onWasmNodeTextMethod, (jint)node_id);
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
            jText = nullptr;
        }

        if (jText) {
            // Get UTF-8 bytes and length
            const jsize jlen = env->GetStringUTFLength(jText); // byte length (modified UTF-8)
            const char* cstr = env->GetStringUTFChars(jText, nullptr);
            len = (uint32_t) jlen;

            // Call WASM alloc(len) to get buffer inside WASM memory
            IM3Function allocFunc = nullptr;
            M3Result res = m3_FindFunction(&allocFunc, runtime, "alloc");
            if (res == m3Err_none) {
                const void* args[1] = { &len };
                res = m3_Call(allocFunc, 1, args);
            }
            if (res == m3Err_none) {
                const void* results[1] = { &strPtr };
                res = m3_GetResults(allocFunc, 1, results);
            }
            if (res != m3Err_none) {
                // Cleanup and bail
                if (cstr) env->ReleaseStringUTFChars(jText, cstr);
                env->DeleteLocalRef(jText);
                if (attached) g_vm->DetachCurrentThread();
                sp[0] = 0;
                return nullptr;
            }

            // Refresh memory pointer (heap may have grown)
            memory = m3_GetMemory(runtime, nullptr, 0);
            // Copy text bytes into WASM memory at strPtr
            memcpy(memory + strPtr, cstr, len);

            // Write the pointer value to the caller-provided out_ptr slot
            // out_ptr_addr is an address INSIDE wasm memory that stores a u32.
            *(uint32_t*)(memory + out_ptr_addr) = strPtr;

            // Cleanup JNI
            env->ReleaseStringUTFChars(jText, cstr);
            env->DeleteLocalRef(jText);
        } else {
            // No string returned: write 0 pointer, 0 length
            *(uint32_t*)(memory + out_ptr_addr) = 0;
            len = 0;
        }
    }

    if (attached) g_vm->DetachCurrentThread();

    // return length in sp[0]
    sp[0] = (uint64_t) len;
    return nullptr;
}

const void* html_node_attr_host(IM3Runtime runtime, IM3ImportContext, uint64_t* sp, void*) {
    uint8_t* memory = m3_GetMemory(runtime, nullptr, 0);
    if (!memory) {
        sp[0] = 0; // length = 0
        return nullptr;
    }

    const uint32_t node_id       = (uint32_t) sp[1]; // node ID
    const uint32_t name_ptr      = (uint32_t) sp[2]; // pointer to attribute name in WASM memory
    const uint32_t name_len      = (uint32_t) sp[3]; // length of attribute name
    const uint32_t out_ptr_addr  = (uint32_t) sp[4]; // where to write pointer to resulting string

    if (!g_vm || !g_onWasmNodeAttrMethod) {
        sp[0] = 0;
        return nullptr;
    }

    JNIEnv* env = nullptr;
    bool attached = false;

    jint s = g_vm->GetEnv((void**)&env, JNI_VERSION);
    if (s == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) == JNI_OK) attached = true;
#else
        if (g_vm->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) == JNI_OK) attached = true;
#endif
    }

    if (!env) {
        sp[0] = 0;
        return nullptr;
    }

    // Copy attribute name from WASM memory into a C string
    const char* str = reinterpret_cast<const char*>(memory + name_ptr);
    std::string attr_name(str, name_len);

    uint32_t len = 0;
    uint32_t strPtr = 0;

    // Call Kotlin/Java method: JNIBridge.onWasmNodeAttr(nodeId, attrName)
    jstring jAttr = (jstring) env->CallStaticObjectMethod(
        g_jniBridgeClass,
        g_onWasmNodeAttrMethod,
        (jint) node_id,
        env->NewStringUTF(attr_name.c_str())
    );

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        jAttr = nullptr;
    }

    if (jAttr) {
        const jsize jlen = env->GetStringUTFLength(jAttr);
        const char* cstr = env->GetStringUTFChars(jAttr, nullptr);
        len = (uint32_t) jlen;

        // Allocate buffer inside WASM memory
        IM3Function allocFunc = nullptr;
        if (m3_FindFunction(&allocFunc, runtime, "alloc") == m3Err_none) {
            const void* args[1] = { &len };
            if (m3_Call(allocFunc, 1, args) == m3Err_none) {
                const void* results[1] = { &strPtr };
                m3_GetResults(allocFunc, 1, results);
            }
        }

        // Refresh memory pointer
        memory = m3_GetMemory(runtime, nullptr, 0);

        // Copy string into WASM memory
        memcpy(memory + strPtr, cstr, len);

        // Write pointer to caller-provided out_ptr
        *(uint32_t*)(memory + out_ptr_addr) = strPtr;

        // Cleanup JNI
        env->ReleaseStringUTFChars(jAttr, cstr);
        env->DeleteLocalRef(jAttr);
    } else {
        // No string returned
        *(uint32_t*)(memory + out_ptr_addr) = 0;
        len = 0;
    }

    if (attached) g_vm->DetachCurrentThread();

    sp[0] = len; // return length
    return nullptr;
}

const void* get_setting_in_group_host(IM3Runtime runtime, IM3ImportContext, uint64_t* sp, void*) {
    uint8_t *memory = m3_GetMemory(runtime, nullptr, 0);
    if (!memory) {
        sp[0] = 0; // length = 0
        return nullptr;
    }

    const uint32_t group_ptr = (uint32_t) sp[1]; // node ID
    const uint32_t group_len = (uint32_t) sp[2]; // pointer to attribute name in WASM memory
    const uint32_t key_ptr = (uint32_t) sp[3]; // length of attribute name
    const uint32_t key_len = (uint32_t) sp[4]; // where to write pointer to resulting string
    const uint32_t out_ptr = (uint32_t) sp[5]; // where to write pointer to resulting string

    if (!g_vm || !g_onWasmNodeAttrMethod) {
        sp[0] = 0;
        return nullptr;
    }

    JNIEnv *env = nullptr;
    bool attached = false;

    jint s = g_vm->GetEnv((void **) &env, JNI_VERSION);
    if (s == JNI_EDETACHED) {
#ifdef __ANDROID__
        if (g_vm->AttachCurrentThread(&env, nullptr) == JNI_OK) attached = true;
#else
        if (g_vm->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) == JNI_OK) attached = true;
#endif
    }

    if (!env) {
        sp[0] = 0;
        return nullptr;
    }

    // get setting value
    const char* group_str = reinterpret_cast<const char*>(memory + group_ptr);
    std::string group(group_str, group_len);
    const char* key_str = reinterpret_cast<const char*>(memory + key_ptr);
    std::string key(key_str, key_len);

    jstring jgroup = env->NewStringUTF(group.c_str());
    jstring jkey   = env->NewStringUTF(key.c_str());

    jobject result = env->CallStaticObjectMethod(g_jniBridgeClass, g_onWasmGetSettingInGroupMethod, jgroup, jkey);

    uint32_t len = 0;      // length of allocated value
    uint32_t wasm_ptr = 0;  // pointer to allocated memory

// Determine type and allocate
    if (env->IsInstanceOf(result, env->FindClass("java/lang/String"))) {
        const jsize jlen = env->GetStringUTFLength((jstring)result);
        const char* cstr = env->GetStringUTFChars((jstring)result, nullptr);
        len = static_cast<uint32_t>(jlen);

        // Allocate in WASM
        IM3Function allocFunc = nullptr;
        if (m3_FindFunction(&allocFunc, runtime, "alloc") == m3Err_none) {
            const void* args[1] = { &len };
            if (m3_Call(allocFunc, 1, args) == m3Err_none) {
                const void* results[1] = { &wasm_ptr };
                m3_GetResults(allocFunc, 1, results);
            }
        }

        memory = m3_GetMemory(runtime, nullptr, 0);
        memcpy(memory + wasm_ptr, cstr, len);

        env->ReleaseStringUTFChars((jstring)result, cstr);
        env->DeleteLocalRef((jstring)result);

    } else {
        // Unsupported type
        len = 0;
        wasm_ptr = 0;
    }

// Write pointer to caller-provided out_ptr
    *(uint32_t*)(memory + out_ptr) = wasm_ptr;

// Store length in sp[0]
    sp[0] = len;

// Cleanup
    env->DeleteLocalRef(jgroup);
    env->DeleteLocalRef(jkey);

    return nullptr;
}


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

void registerHostFunctions(WasmInstance* instance) {
    M3Result result = m3_LinkRawFunction(
            instance->module,
            "env",
            "log_host",
            "v(ii)",
            log_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link log: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "request_host",
            "i(iii)",   // example signature: returns pointer (p), takes three ints (i i i)
            request_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link request_host: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "html_parse_host",
            "i(ii)",   // example signature: returns pointer (p), takes three ints (i i i)
            html_parse_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link html_parse_host: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "html_query_selector_host",
            "i(iii)",   // example signature: returns pointer (p), takes three ints (i i i)
            html_query_selector_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link html_query_selector_host: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "html_query_selector_all_host",
            "i(iiii)",   // example signature: returns pointer (p), takes three ints (i i i)
            html_query_selector_all_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link html_query_selector_all_host: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "html_node_text_host",
            "i(ii)",   // example signature: returns pointer (p), takes three ints (i i i)
            html_node_text_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link html_node_text_host: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "html_node_attr_host",
            "i(iiii)",   // example signature: returns pointer (p), takes three ints (i i i)
            html_node_attr_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link html_node_attr_host: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "html_node_query_selector_host",
            "i(iii)",   // example signature: returns pointer (p), takes three ints (i i i)
            html_node_query_selector_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link html_node_query_selector_host: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "html_node_query_selector_all_host",
            "i(iiii)",   // example signature: returns pointer (p), takes three ints (i i i)
            html_node_query_selector_all_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link html_node_query_selector_all_host: %s", result);
    }

    result = m3_LinkRawFunction(
            instance->module,
            "env",
            "get_setting_in_group_host",
            "i(iiiii)",   // example signature: returns pointer (p), takes three ints (i i i)
            get_setting_in_group_host
    );
    if (result != m3Err_none) {
        LOGE("Failed to link get_setting_in_group_host: %s", result);
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_inumaki_relaywasm_WasmRuntime_loadNativeModule(JNIEnv *env, jobject thiz, jbyteArray wasmArray) {
    jsize jlength = env->GetArrayLength(wasmArray);
    if (jlength <= 0) return 0;

    auto length = static_cast<uint32_t>(jlength);
    auto* wasm_bytes = static_cast<uint8_t*>(malloc(length));
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

    registerHostFunctions(instance);

    return reinterpret_cast<jlong>(instance);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_inumaki_relaywasm_WasmModule_callNativeFunction(JNIEnv* env, jobject thiz, jlong ptr, jstring jname) {
    auto* instance = reinterpret_cast<WasmInstance*>(ptr);
    if (!instance) return nullptr;

    const char* name = env->GetStringUTFChars(jname, nullptr);
    if (!name) return nullptr;

    IM3Function func;
    M3Result result = m3_FindFunction(&func, instance->runtime, name);
    env->ReleaseStringUTFChars(jname, name);
    if (result != m3Err_none) return nullptr;

    // Call WASM function (no args)
    result = m3_CallArgv(func, 0, nullptr);
    if (result != m3Err_none) return nullptr;

    // Get pointer to the returned null-terminated JSON string (as u8*)
    uint32_t string_offset;
    const void* retptrs[1] = { &string_offset };
    result = m3_GetResults(func, 1, retptrs);
    if (result != m3Err_none) return nullptr;

    // Get WASM linear memory base
    uint8_t* memory = m3_GetMemory(instance->runtime, nullptr, 0);
    if (!memory) return nullptr;

    // Pointer to null-terminated string in WASM memory
    const char* json_str = reinterpret_cast<const char*>(memory + string_offset);

    // Calculate length of null-terminated string
    size_t len = 0;
    while (json_str[len] != '\0') {
        ++len;
    }

    // Create Java string from UTF-8 bytes
    // Use NewStringUTF only if the string is valid UTF-8 and null-terminated
    jstring retString = env->NewStringUTF(json_str);

    // Optional: log the JSON string for debugging
    // LOGI("Returned JSON: %s", json_str);

    return retString;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_inumaki_relaywasm_WasmRuntime_getMemory(JNIEnv *env, jobject thiz, jlong ptr) {
    auto* instance = reinterpret_cast<WasmInstance*>(ptr);

    uint32_t size = 0;
    uint8_t* memory = m3_GetMemory(instance->runtime, &size, 0);

    return env->NewDirectByteBuffer(memory, size);
}
