package com.jdailer.feature.call.recording

data class CallSession(
    val callId: String,
    val number: String?,
    val direction: String,
    val userConsented: Boolean
)
