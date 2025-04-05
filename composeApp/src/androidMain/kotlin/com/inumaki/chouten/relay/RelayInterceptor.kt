package com.inumaki.chouten.relay

import android.util.Log
import com.caoccao.javet.annotations.V8Function
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Headers.Companion.toHeaders
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RequestInterceptor {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Increase connection timeout
        .readTimeout(30, TimeUnit.SECONDS) // Increase read timeout
        .writeTimeout(30, TimeUnit.SECONDS) // Increase write timeout
        .build()

    @V8Function
    fun consoleLog(message: String) {
        Log.d("Relay", message)

        sendLogToLogServer(message)

        // TODO: add message to local logging manager
    }

    private fun sendLogToLogServer(message: String) {
        // TODO: send message to log server
        val url = "http://192.168.1.163:3000/log" // Replace with the actual IP

        val request = Request.Builder()
            .url(url)
            .method("POST", message.toRequestBody("text/plain".toMediaTypeOrNull()))
            .build()

        Log.d("Log Server", "Sending to log server.")

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Failed to send log: ${response.message}")
                } else {
                    println("Log sent successfully")
                }
            }
        } catch (e: Exception) {
            Log.e("Log Server", e.message.toString())
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
        val headersList = headers.toHeaders()

        val request = Request.Builder()
            .url(url)
            .method(method.uppercase(), null)
            .headers(headersList)
            .build()

        Log.d("Relay", "URL: $url")

        return try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""

                println("Body: $bodyString")

                JSONObject().apply {
                    put("statusCode", response.code)
                    put("headers", JSONObject(response.headers.toMap()))
                    put("contentType", response.header("Content-Type") ?: "unknown")
                    put("body", bodyString)
                }.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            JSONObject().apply {
                put("statusCode", 500)
                put("headers", JSONObject())
                put("contentType", "application/json")
                put("body", "Error: ${e.message}")
            }.toString()
        }
    }
}