package com.mcoder.data.remote

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic SSE client for streaming agent responses.
 */
@Singleton
class SseAgentClient @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    fun stream(url: String, token: String?, message: String): Flow<String> = callbackFlow {
        val payload = JSONObject()
            .put("message", message)
            .toString()
        val body = payload.toRequestBody(JSON_MEDIA)
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        val factory = EventSources.createFactory(client)
        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    close()
                } else {
                    trySend(extractText(data))
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: okhttp3.Response?
            ) {
                close(t)
            }
        }
        val eventSource = factory.newEventSource(request, listener)
        awaitClose { eventSource.cancel() }
    }

    private fun extractText(data: String): String {
        return try {
            val obj = JSONObject(data)
            when {
                obj.has("delta") -> obj.optString("delta")
                obj.has("content") -> obj.optString("content")
                obj.has("text") -> obj.optString("text")
                else -> data
            }
        } catch (_: Exception) {
            data
        }
    }

    private companion object {
        val JSON_MEDIA = "application/json".toMediaType()
    }
}
