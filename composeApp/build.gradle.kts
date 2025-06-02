import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.1.20"
    // id("maven-publish")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.javet.v8.android)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.serialization)
            implementation(libs.kermit)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.navigation.compose)
            implementation(libs.font.awesome)
            implementation(libs.kotlinx.serialization.json)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.javet)
            implementation(libs.javet.native.linux.x86.x4) // for Linux
            implementation(libs.javet.native.macos.x86.x4) // for macOS (x86_64)
            implementation(libs.javet.native.macos.arm64) // for macOS (arm64)
            implementation(libs.javet.native.windows.x86.x4) // for Windows
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.io.jvm)
        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }

    /*
    targets.withType<KotlinNativeTarget> {
        compilations.getByName("main") {
            cinterops {
                val wasm3 by creating {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/wasm3.def"))
                    packageName("wasm3")
                    compilerOpts("-I${projectDir}/build/wasm3/include")
                    includeDirs.allHeaders(project.file("src/nativeInterop/cinterop/wasm3/source"))
                }
            }
        }

        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

     */
}

/*
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.inumaki"
            artifactId = "kmp-wasm3"
            version = "1.0.0"

            from(components["kotlin"])
        }
    }
    repositories {
        maven {
            url = uri("../repo")  // Local repository
        }
    }
}

tasks.withType<KotlinNativeLink> {
    dependsOn("buildWasm3")  // Ensure wasm3 is built before linking
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess>().configureEach {
    if (name.contains("wasm3", ignoreCase = true)) {
        println("Running cinterop for wasm3 with the following paths:")
        println("Header: ${project.file("src/nativeInterop/cinterop/wasm3/wasm3.h")}")
        println("Def file: ${project.file("src/nativeInterop/cinterop/wasm3.def")}")
        println("Linker options: -L${projectDir}/build/wasm3")
        dependsOn("buildWasm3")
    }
}

// Task to build wasm3 (static library)
tasks.register("buildWasm3") {
    group = "build"
    description = "Builds the wasm3 static library"

    val wasm3SourceDir = file("src/nativeInterop/cinterop/wasm3/source")
    val buildDir = file("build/wasm3")

    outputs.dir(buildDir)

    doLast {
        buildDir.mkdirs()

        exec {
            commandLine(
                "clang",  // or gcc, depending on your system
                "-c",
                "$wasm3SourceDir/wasm3.c",
                "-I$wasm3SourceDir",
                "-o",
                "$buildDir/wasm3.o"
            )
        }

        exec {
            commandLine(
                "ar",
                "rcs",
                "$buildDir/libwasm3.a",
                "$buildDir/wasm3.o"
            )
        }

        // Optionally copy headers to a standard include location (or just leave them in source)
        copy {
            from("$wasm3SourceDir/wasm3.h")
            into("$buildDir/include")
        }
    }
}
*/
android {
    namespace = "com.inumaki.chouten"
    compileSdk = 35 // libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.inumaki.chouten"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
/*
        externalNativeBuild {
            cmake {
                cppFlags += "-I${android.ndkDirectory}/sysroot/usr/include"
            }
        }
        */
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets["main"].apply {
        res.srcDirs("src/androidMain/res", "src/commonMain/resources/res")
        // 3..
        assets.srcDirs("src/commonMain/assets")
    }
    /*
    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    */
}

dependencies {
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.identity.jvm)
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.inumaki.chouten.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.inumaki.chouten"
            packageVersion = "1.0.0"
        }
    }
}