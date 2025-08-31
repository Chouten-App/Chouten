package com.inumaki.chouten.relay

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.caoccao.javet.interception.logging.JavetStandardConsoleInterceptor
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueArray
import com.caoccao.javet.values.reference.V8ValueObject
import com.caoccao.javet.values.reference.V8ValuePromise
import com.inumaki.relaywasm.models.DiscoverData
import com.inumaki.relaywasm.models.DiscoverSection
import com.inumaki.chouten.helpers.convertMapToDiscoverSections
import com.inumaki.relaywasm.models.Label
import com.inumaki.relaywasm.models.Titles
import java.io.BufferedReader

actual object Relay {
    actual val version: String = "0.0.1"

    private lateinit var appContext: Context
    private lateinit var v8Runtime: V8Runtime

    var commonJs = ""

    actual fun init() {
        v8Runtime = V8Host.getV8Instance().createV8Runtime()

        val javetStandardConsoleInterceptor = JavetStandardConsoleInterceptor(v8Runtime)
        javetStandardConsoleInterceptor.register(v8Runtime.globalObject)

        commonJs = loadCommonJs()

        // fetch code.js from Documents folder
        val codeJs = loadCodeJs() ?: return

        Log.d("Relay", "Code.js initialized.")

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

    fun setContext(context: Context) {
        if (!this::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    actual suspend fun discover(): List<DiscoverSection> {
        val promise = v8Runtime.getExecutor("instance.discover().then(res => res)").execute<V8ValuePromise>()

        v8Runtime.await() // Make sure this doesn't block the UI

        if (promise == null || promise.isRejected) {
            println("Error: Promise was rejected")
            return emptyList()
        }

        val returnObject = promise.getResult<V8ValueArray>() ?: return emptyList()

        return v8ArrayToDiscoverArray(returnObject)
    }

    private fun loadCodeJs(): String? {
        val sharedPrefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val folderUriString = sharedPrefs.getString("selected_directory", null) ?: return null
        val folderUri = Uri.parse(folderUriString)

        val folder = DocumentFile.fromTreeUri(appContext, folderUri) ?: return null
        val codeJsFile = folder.findFile("code.js") ?: return null

        appContext.contentResolver.openInputStream(codeJsFile.uri)?.use { inputStream ->
            return inputStream.bufferedReader().use { it.readText() }
        }

        return null
    }

    private fun loadCommonJs(): String {
        return appContext.assets.open("common.js").bufferedReader().use(BufferedReader::readText)
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

            // sections.add(DiscoverSection(title = title, section_type = type, list = list))
        }

        println("SECTIONS CONVERTED: $sections")

        return sections
    }
}
