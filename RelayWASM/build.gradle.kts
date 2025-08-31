plugins {
    kotlin("multiplatform") version "2.2.0" // or newer
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
    id("com.android.library")
}

kotlin {
    androidTarget()
    jvm("desktop") // JVM for desktop apps

    /*
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "RelayWasm"
        }
    }*/

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Add KMP-compatible dependencies here
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("com.fleeksoft.ksoup:ksoup:0.2.5")
                implementation("com.akuleshov7:ktoml-core:0.7.1")
                implementation("com.akuleshov7:ktoml-file:0.7.1")
            }
        }

        val androidMain by getting
        val desktopMain by getting
    }
}

tasks.register<Exec>("buildWasmDll") {
    group = "build"
    description = "Compile wasm3 C sources and RelayWASM.cpp into RelayWASM.dll using mingw-w64"

    workingDir = project.projectDir

    val projectDirWSL = project.projectDir.absolutePath
        .replace("\\", "/")
        .replace(Regex("^([A-Za-z]):")) { "/mnt/${it.groupValues[1].lowercase()}" }

    doFirst {
        file("build/libs").mkdirs()
    }

    // Use a single-line command or escape carefully
    environment("PROJECT_DIR", projectDirWSL)
    val bashCommand = if (System.getProperty("os.name").lowercase().contains("windows")) {
        listOf("wsl", "bash", "./src/main/cpp/buildWasmDll.sh")
    } else {
        listOf("bash", "./src/main/cpp/buildWasmDll.sh")
    }

    commandLine(bashCommand)

    // Fail the build if script exits non-zero
    isIgnoreExitValue = false
}



android {
    namespace = "com.inumaki.relaywasm"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }

    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}
