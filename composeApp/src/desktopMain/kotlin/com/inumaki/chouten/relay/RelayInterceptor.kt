package com.inumaki.chouten.relay

import com.caoccao.javet.annotations.V8Function
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class RequestInterceptor {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Increase connection timeout
        .readTimeout(30, TimeUnit.SECONDS) // Increase read timeout
        .writeTimeout(30, TimeUnit.SECONDS) // Increase write timeout
        .build()

    @V8Function
    fun consoleLog(message: String) {
        println("Relay: $message")
        // sendLogToLogServer(message)
        // TODO: add message to local logging manager
    }

    private fun sendLogToLogServer(message: String) {
        // Replace with the actual logging server IP
        val url = "http://192.168.1.163:3000/log"

        val request = Request.Builder()
            .url(url)
            .post(message.toRequestBody("text/plain".toMediaTypeOrNull()))
            .build()

        println("Sending to log server.")

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Failed to send log: ${response.message}")
                } else {
                    println("Log sent successfully")
                }
            }
        } catch (e: Exception) {
            println("Log Server Error: ${e.message}")
        }
    }

    @V8Function
    fun request(url: String, method: String, headers: Map<String, String> = mapOf()): String {
        val deferred = CoroutineScope(Dispatchers.IO).async {
            performRequest(url, method, headers)
        }

        return runBlocking { deferred.await() }
    }

    private fun performRequest(url: String, method: String, headers: Map<String, String> = mapOf()): String {
        val headersList = Headers.Builder().apply {
            headers.forEach { (key, value) -> add(key, value) }
        }.build()

        val request = Request.Builder()
            .url(url)
            .method(method.uppercase(), null)
            .headers(headersList)
            .build()

        println("Relay Request: $url")

        return try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""

                println("Response Body: $bodyString")

                val jsonResponse = buildJsonObject {
                    put("statusCode", JsonPrimitive(response.code))
                    put("headers", JsonObject(response.headers.toMap().mapValues { JsonPrimitive(it.value) }))
                    put("contentType", JsonPrimitive(response.headers["Content-Type"] ?: "unknown"))
                    put("body", JsonPrimitive(bodyString))
                }

                Json.encodeToString(JsonObject.serializer(), jsonResponse)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val jsonResponse = buildJsonObject {
                put("statusCode", 500)
                put("headers", JsonObject(emptyMap()))
                put("contentType", JsonPrimitive("application/json"))
                put("body", "Error: ${e.message}")
            }

            Json.encodeToString(JsonObject.serializer(), jsonResponse)
        }
    }
}
