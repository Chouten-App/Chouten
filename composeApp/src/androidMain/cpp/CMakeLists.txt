cmake_minimum_required(VERSION 3.10.2)

project("wasm3wrapper")

# Get all .c files in the wasm3 source directory
file(GLOB WASM3_SOURCES
        "../../nativeInterop/cinterop/wasm3/source/*.c"
        "wasm3wrapper.cpp"
)

# Add the library
add_library(wasm3wrapper SHARED ${WASM3_SOURCES})

# Optionally include directories
target_include_directories(wasm3wrapper PRIVATE
        ../../nativeInterop/cinterop/wasm3/source
        $ENV{ANDROID_NDK}/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include
        $ENV{ANDROID_NDK}/sysroot/usr/include
)

target_compile_options(wasm3wrapper PRIVATE
        -Wno-error
)

# Link libraries if needed (like -llog)
if(ANDROID)
    target_link_libraries(wasm3wrapper
            log
            atomic
            m
    )
endif()
