package com.inumaki.chouten.relay

import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.interception.logging.JavetStandardConsoleInterceptor
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.values.reference.V8ValueArray
import com.caoccao.javet.values.reference.V8ValueObject
import com.caoccao.javet.values.reference.V8ValuePromise
import com.inumaki.chouten.Models.DiscoverData
import com.inumaki.chouten.Models.DiscoverSection
import com.inumaki.chouten.Models.Label
import com.inumaki.chouten.Models.Titles
import java.io.File
import java.io.InputStream
import java.util.Locale

actual object Relay {
    actual val version: String = "0.0.1"

    private lateinit var v8Runtime: V8Runtime

    private var commonJs: String = ""

    actual fun init() {
        v8Runtime = V8Host.getV8Instance().createV8Runtime()

        val javetStandardConsoleInterceptor = JavetStandardConsoleInterceptor(v8Runtime)
        javetStandardConsoleInterceptor.register(v8Runtime.globalObject)

        commonJs = loadCommonJs()

        // fetch code.js from Documents folder
        val codeJs = loadCodeJs() ?: return

        println("Relay: Code.js initialized.")

        val relayInterceptor = RequestInterceptor()

        v8Runtime.createV8ValueObject().use { v8ValueObject ->
            v8Runtime.globalObject.set("RelayBridge", v8ValueObject)
            v8ValueObject.bind(relayInterceptor)
        }

        v8Runtime.getExecutor(commonJs.trimIndent()).executeVoid()
        v8Runtime.getExecutor(codeJs.trimIndent()).executeVoid()

        // Next: Make it load a proper module
        v8Runtime.getExecutor("const instance = new source.default();".trimIndent()).executeVoid()
    }

    actual suspend fun discover(): List<DiscoverSection> {
        return try {
            val promise = v8Runtime
                .getExecutor("instance.discover()")
                .execute<V8ValuePromise>()

            if (promise == null) {
                println("Error: Promise is null")
                return emptyList()
            }

            // Await JS promise resolution
            v8Runtime.await()

            if (promise.isRejected) {
                val reasonValue = promise.getString("reason")
                val message = reasonValue?.toString() ?: "Unknown JS rejection reason"
                println("Promise was rejected: $message")
                return emptyList()
            }

            val returnObject = promise.getResult<V8ValueArray>()
            if (returnObject == null) {
                println("Error: Result is null or not an array")
                return emptyList()
            }

            return v8ArrayToDiscoverArray(returnObject)

        } catch (e: JavetException) {
            println("Caught JavetException: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            println("Caught unexpected exception: ${e.message}")
            emptyList()
        }
    }


    private fun loadCommonJs(): String {
        val inputStream: InputStream? = this::class.java.getResourceAsStream("/common.js")

        return inputStream?.bufferedReader()?.use { it.readText() } ?: ""
    }

    private fun loadCodeJs(): String? {
        val documentsDir = getDocumentsDirectory()
        val choutenFolder = File(documentsDir, "ChoutenData")
        val codeJsFile = File(choutenFolder, "code.js")

        if (codeJsFile.exists()) {
            // Read the file content
            return codeJsFile.readText()
        }
        return null
    }

    private
    fun getDocumentsDirectory(): String {
        val userHome = System.getProperty("user.home")
        return when {
            // macOS or Linux
            System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac") -> "$userHome/Documents"
            System.getProperty("os.name").lowercase(Locale.getDefault()).contains("linux") -> "$userHome/Documents"
            // Windows
            System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win") -> {
                val userDocuments = System.getenv("USERPROFILE") + "\\Documents"
                userDocuments
            }
            else -> throw IllegalStateException("Unsupported OS")
        }
    }

    @OptIn(kotlin. uuid. ExperimentalUuidApi::class)
    private fun v8ArrayToDiscoverArray(v8Array: V8ValueArray): List<DiscoverSection> {
        val sections = mutableListOf<DiscoverSection>()

        for (i in 0 until v8Array.length) {
            val sectionObj = v8Array.get<V8ValueObject>(i) ?: continue

            val title = sectionObj.getString("title")
            val type = sectionObj.getInteger("type")

            val list = mutableListOf<DiscoverData>()
            try {
                val listArray = sectionObj.get<V8ValueArray>("data")

                println(listArray.length)

                for (j in 0 until listArray.length) {
                    println(j)
                    val dataObj = listArray.get<V8ValueObject>(j)

                    val url = dataObj.getString("url")
                    val titlesObj = dataObj.get<V8ValueObject>("titles")
                    val poster = dataObj.getString("poster")
                    val banner: String? = dataObj.getString("banner")
                    val description = dataObj.getString("description")
                    val indicator: String? = dataObj.getString("indicator")
                    val current: Int? = dataObj.getInteger("current")
                    val total: Int? = dataObj.getInteger("total")

                    println("FOUND: $poster")

                    val titles = Titles(
                        primary = titlesObj.getString("primary"),
                        secondary = "" // titlesObj.getString("secondary")
                    )

                    val label = Label(
                        text = "",
                        color = ""
                    )

                    list.add(
                        DiscoverData(
                            url = url,
                            titles = titles,
                            poster = poster,
                            banner = banner,
                            description = description,
                            label = label,
                            indicator = indicator,
                            isWidescreen = false,
                            current = current,
                            total = total
                        )
                    )
                }
            } catch (e: Exception) {
                println(e.message)
            }

            sections.add(DiscoverSection(title = title, type = type, list = list))
        }

        println("SECTIONS CONVERTED: $sections")

        return sections
    }
}
