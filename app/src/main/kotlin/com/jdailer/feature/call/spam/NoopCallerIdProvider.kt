package com.jdailer.feature.call.spam

class NoopCallerIdProvider : RemoteCallerIdProvider {
    override suspend fun lookup(normalizedNumber: String): CallerIdRemoteProfile? = null
}
