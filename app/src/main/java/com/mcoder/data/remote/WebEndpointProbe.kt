package com.mcoder.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Probes local/remote web endpoints to see if they are reachable.
 */
@Singleton
class WebEndpointProbe @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .build()

    suspend fun isReachable(url: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).get().build()
        runCatching { client.newCall(request).execute().use { it.isSuccessful } }
            .getOrDefault(false)
    }
}
