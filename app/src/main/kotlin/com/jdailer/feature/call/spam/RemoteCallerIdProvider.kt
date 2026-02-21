package com.jdailer.feature.call.spam

data class CallerIdRemoteProfile(
    val displayName: String?,
    val city: String?,
    val carrier: String?,
    val spamScore: Int,
    val isSpam: Boolean,
    val reason: String?
)

interface RemoteCallerIdProvider {
    suspend fun lookup(normalizedNumber: String): CallerIdRemoteProfile?
}
