package com.jdailer.feature.call.recording.domain.model

data class RecordingPolicyDecision(
    val canStart: Boolean,
    val reason: String? = null
)
