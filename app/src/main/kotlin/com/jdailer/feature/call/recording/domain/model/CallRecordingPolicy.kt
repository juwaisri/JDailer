package com.jdailer.feature.call.recording.domain.model

data class CallRecordingPolicy(
    val maxRecordingsPerDay: Int = 40,
    val requireExplicitConsent: Boolean = true,
    val redactCallerNumberInMetadata: Boolean = true,
    val minCallerIdLength: Int = 3
)
