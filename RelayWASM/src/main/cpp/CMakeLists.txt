
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("RelayWASM")

# Get all .c files in the wasm3 source directory
file(GLOB WASM3_SOURCES
        "wasm3/source/*.c"
        "RelayWASM.cpp"
)

# Add the library
add_library(RelayWASM SHARED ${WASM3_SOURCES})

# Optionally include directories
target_include_directories(RelayWASM PRIVATE
        wasm3/source
)

target_compile_options(RelayWASM PRIVATE
        -Wno-error
)

# Link libraries if needed (like -llog)
if(ANDROID)
    target_link_libraries(RelayWASM
            log
            atomic
            m
    )
endif()