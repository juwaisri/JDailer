package com.jdailer.feature.call.spam

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class HttpCallerIdProvider(
    private val baseUrl: String,
    private val client: OkHttpClient
) : RemoteCallerIdProvider {
    private val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    private val loggingClient = client.newBuilder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    override suspend fun lookup(normalizedNumber: String): CallerIdRemoteProfile? {
        val uri = buildLookupUri(normalizedNumber) ?: return null
        val request = Request.Builder()
            .url(uri)
            .get()
            .addHeader("Accept", "application/json")
            .build()

        return runCatching {
            loggingClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@use null
                }
                val rawPayload = response.body?.string().orEmpty()
                if (rawPayload.isBlank()) {
                    return@use null
                }
                parsePayload(rawPayload)
            }
        }.getOrNull()
    }

    private fun buildLookupUri(normalizedNumber: String): String? {
        if (normalizedNumber.isBlank()) return null
        val base = normalizedBaseUrl.toHttpUrlOrNull() ?: return null
        val normalized = normalizedNumber.filter { it.isDigit() || it == '+' }
        return base.newBuilder()
            .addPathSegment("lookup")
            .addQueryParameter("number", normalized)
            .addQueryParameter("format", "json")
            .addQueryParameter("features", "spam,caller")
            .build()
            .toString()
    }

    private fun parsePayload(rawPayload: String): CallerIdRemoteProfile? {
        val root = runCatching { JSONObject(rawPayload) }.getOrNull() ?: return null
        val payload = when {
            root.has("data") && root.optJSONObject("data") != null -> root.getJSONObject("data")
            root.has("result") && root.optJSONObject("result") != null -> root.getJSONObject("result")
            else -> root
        }

        val isSpam = readBoolean(payload, listOf("isSpam", "spam", "is_spam"))
        val spamScore = readInt(payload, listOf("spamScore", "score", "spam_score")).coerceIn(0, 100)
        val displayName = readString(payload, listOf("displayName", "name", "callerName"))
        val city = readString(payload, listOf("city", "location"))
        val carrier = readString(payload, listOf("carrier"))
        val reason = readString(payload, listOf("reason", "label"))

        return CallerIdRemoteProfile(
            displayName = displayName,
            city = city,
            carrier = carrier,
            spamScore = spamScore,
            isSpam = isSpam || spamScore >= 80,
            reason = reason
        )
    }

    private fun readBoolean(payload: JSONObject, keys: List<String>): Boolean {
        keys.forEach { key ->
            if (!payload.has(key)) return@forEach

            if (payload.isNull(key)) return@forEach
            val value = payload.opt(key)
            if (value is Boolean) return value
            if (value is String) {
                when (value.lowercase()) {
                    "true", "1", "yes" -> return true
                    "false", "0", "no" -> return false
                }
            }
            if (value is Number) {
                return value.toInt() == 1
            }
        }
        return false
    }

    private fun readString(payload: JSONObject, keys: List<String>): String? {
        keys.forEach { key ->
            if (!payload.has(key) || payload.isNull(key)) return@forEach
            val value = payload.optString(key, "").trim()
            if (value.isNotBlank()) return value
        }
        return null
    }

    private fun readInt(payload: JSONObject, keys: List<String>): Int {
        keys.forEach { key ->
            if (!payload.has(key) || payload.isNull(key)) return@forEach
            val value = payload.opt(key)
            when (value) {
                is Number -> return value.toInt()
                is String -> return value.toIntOrNull() ?: return@forEach
            }
            val fallback = payload.optInt(key, -1)
            if (fallback >= 0) return fallback
        }
        return 0
    }
}
