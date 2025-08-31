package com.inumaki.relaywasm

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

object JNIBridge {
    private val executor = Executors.newSingleThreadExecutor()

    @JvmStatic
    fun onWasmLog(msg: String) {
        println("Kotlin received WASM log: $msg")

        LogManager.log("System", msg)
    }

    @JvmStatic
    fun onWasmHtmlParse(html: String): Int {
        println("Parse")
        return HtmlHost.parse(html)
    }

    @JvmStatic
    fun onWasmHtmlQuerySelector(selector: String, docId: Int): Int {
        return HtmlHost.querySelector(docId, selector)
    }

    @JvmStatic
    fun onWasmHtmlQuerySelectorAll(selector: String, docId: Int): IntArray {
        val nodes = HtmlHost.querySelectorAll(docId, selector) // List<HtmlNode> or List<Int>
        return nodes.toIntArray()
    }

    @JvmStatic
    fun onWasmNodeQuerySelector(selector: String, docId: Int): Int {
        return HtmlHost.nodeQuerySelector(docId, selector)
    }

    @JvmStatic
    fun onWasmNodeQuerySelectorAll(selector: String, elementId: Int): IntArray {
        val nodes = HtmlHost.querySelectorAll(elementId, selector) // List<HtmlNode> or List<Int>
        return nodes.toIntArray()
    }

    @JvmStatic
    fun onWasmNodeText(elementId: Int): String {
        return HtmlHost.nodeText(elementId)
    }

    @JvmStatic
    fun onWasmNodeAttr(elementId: Int, attr: String): String {
        return HtmlHost.nodeAttr(elementId, attr)
    }

    @JvmStatic
    fun onWasmRequest(url: String, method: Int): String {
        println("Request started: $url, $method")
        LogManager.log("System", "Request started: $url, $method")

        val task = FutureTask(Callable {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = when (method) {
                    0 -> "GET"
                    1 -> "POST"
                    2 -> "PUT"
                    3 -> "DELETE"
                    else -> "GET"
                }
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                val inputStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val body = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                LogManager.log("System", body)
                body
            } catch (e: Exception) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        })

        executor.execute(task)

        // Block until result is ready (but NOT on main thread)
        return task.get()
    }
}