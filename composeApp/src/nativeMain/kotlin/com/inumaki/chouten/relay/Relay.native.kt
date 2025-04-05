package com.inumaki.chouten.relay

import com.inumaki.chouten.Models.DiscoverData
import com.inumaki.chouten.Models.DiscoverSection
import com.inumaki.chouten.helpers.PlatformLogger
import com.inumaki.chouten.helpers.convertMapToDiscoverSections
import com.inumaki.chouten.models.Label
import com.inumaki.chouten.models.Titles
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ktor.client
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocalizedDescriptionKey
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.setValue
import platform.Foundation.stringWithContentsOfURL
import platform.JavaScriptCore.JSContext
import platform.JavaScriptCore.JSValue
import platform.JavaScriptCore.objectForKeyedSubscript
import platform.JavaScriptCore.setObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


suspend fun JSContext.callAsyncFunction(key: String): JSValue = suspendCancellableCoroutine { continuation ->
    val onFulfilled: (JSValue?) -> Unit = { value ->
        PlatformLogger.log("Call Async", "$value")
        value?.let {
            continuation.resume(value)
        }
    }

    val onRejected: (JSValue?) -> Unit = { reason ->
        val error = Exception("JavaScript Error: ${reason?.toString() ?: "Unknown"} (key: $key)")
        continuation.resumeWithException(error)
    }

    val fulfilledFunction = JSValue.valueWithObject({ args: Array<JSValue?> ->
        PlatformLogger.log("Call Async", "$args")
        onFulfilled(JSValue.valueWithObject(args, inContext = this))
    }, this)

    val rejectedFunction = JSValue.valueWithObject({ args: Array<JSValue?> ->
        onRejected(args.firstOrNull())
    }, this)

    val jsPromise = this.evaluateScript(key)

    if (jsPromise == null) {
        continuation.resumeWithException(Exception("Failed to evaluate script: $key"))
        return@suspendCancellableCoroutine
    }

    PlatformLogger.log("Call async", "$jsPromise")

    jsPromise.invokeMethod("then", withArguments = listOf(fulfilledFunction, rejectedFunction))
}

actual object Relay {
    actual val version: String = "0.0.1"

    private var jsContext = JSContext()

    actual fun init() {
        PlatformLogger.log("Relay", "Initialized Relay.")

        jsContext.exceptionHandler = { context, exception ->
            PlatformLogger.error("Relay", "${exception?.description}")
            val stackTrace = exception?.objectForKeyedSubscript("stack")
            PlatformLogger.error("Relay", "Stack: $stackTrace")
        }

        // Load code.js
        loadCodeJs()

        evaluateScript("const instance = new source.default();")

        PlatformLogger.log("Relay", "Instance created.")

        // Add JSBridge
        bridgeJSKotlin()
    }

    actual suspend fun discover(): List<DiscoverSection> {
        val value = callAsyncFunc("instance.discover()")

        PlatformLogger.log("Relay", "Finished discover.")
        PlatformLogger.log("Relay", "$value")

        value?.let {
            val returnValue = jsValueToDiscoverArray(value)

            return returnValue
        }

        return emptyList()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun jsValueToDiscoverArray(value: JSValue): List<DiscoverSection> {
        val array = value.toArray()

        val result: MutableList<DiscoverSection> = mutableListOf()
        array?.let {
            for (item in array) {
                val jsItem = item as Map<String, Any>

                result.add(
                    convertMapToDiscoverSections(jsItem)
                )
            }
        }

        return result
    }

    private suspend fun callAsyncFunc(key: String): JSValue? {
        return jsContext.callAsyncFunction(key)
    }

    private fun evaluateScript(script: String): JSValue? {
        return jsContext.evaluateScript(script)
    }

    private fun bridgeJSKotlin() {
        consoleBridge()
        sendRequestBridge()
    }

    private fun consoleBridge() {
        val consoleLog: (String) -> Unit = { message ->
            PlatformLogger.log("Console", message)
        }

        val consoleError: (String) -> Unit = { message ->
            PlatformLogger.error("Console", message)
        }

        jsContext.setObject(
            consoleLog,
            forKeyedSubscript = "consoleLog" as NSString
        )

        jsContext.setObject(
            consoleError,
            forKeyedSubscript = "consoleError" as NSString
        )

        evaluateScript("""
            console.log = function(message) {
                consoleLog(message);
            };

            console.error = function(message) {
                consoleError(message);
            };
        """)
    }

    private fun sendRequestBridge() {
        val sendRequest: (String, String, Map<String, String>, String?) -> JSValue? = { url, method, headers, body ->
            val promise = JSValue.valueWithNewPromiseInContext(jsContext) { resolve, reject ->
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val response = sendRequest(url, method, headers, body)

                        PlatformLogger.log("Relay", "response: $url")
                        resolve?.callWithArguments(listOf(response))
                    } catch (e: Exception) {
                        PlatformLogger.log("Relay", "Exception occurred: ${e.message}")
                        reject?.callWithArguments(listOf(e.message))
                    }
                }
            }

            promise
        }

        jsContext.setObject(
            sendRequest,
            forKeyedSubscript = "request" as NSString
        )
    }

    private suspend fun sendRequest(url: String, method: String, headers: Map<String, String> = emptyMap(), body: String? = null): JSValue {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get(url) {
                    headers.forEach { (key, value) -> header(key, value) }
                }

                val bodyResponse = response.bodyAsText()

                val jsValue = jsContext.objectForKeyedSubscript("Object")?.constructWithArguments(
                    emptyList<String>()
                )

                jsValue?.setObject(bodyResponse, forKeyedSubscript = "body")
                jsValue?.setObject(response.status.value, forKeyedSubscript = "statusCode")
                jsValue?.setObject(response.headers, forKeyedSubscript = "headers")

                return@withContext jsValue ?: JSValue()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun loadCodeJs() {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        )
        val documentsUrl = paths.first() as NSURL

        val jsUrl = documentsUrl.URLByAppendingPathComponent("code.js")


        jsUrl?.let {
            val jsString = NSString.stringWithContentsOfURL(url = it)

            evaluateScript(jsString.toString())
        }
    }
}