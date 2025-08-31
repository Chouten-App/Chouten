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
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.1.20"
}

tasks.withType<Jar>().configureEach {
    dependsOn(":RelayWASM:buildWasmDll")
}

tasks.withType<JavaExec>().configureEach {
    dependsOn(":RelayWASM:buildWasmDll")
}

tasks.withType<JavaExec>().configureEach {
    val libDir = System.getProperty("user.home") + "/RelayWASMLibs"
    systemProperty("java.library.path", libDir)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    /*
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }*/

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
            implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
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
            implementation("dev.chrisbanes.haze:haze:1.6.10")
            implementation("dev.chrisbanes.haze:haze-materials:1.6.10")

            implementation(project(":RelayWASM"))
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
        /*
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }*/
    }
}

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