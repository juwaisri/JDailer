package com.jdailer.feature.voip.domain.model

data class VoipSession(
    val callId: String,
    val address: String,
    val state: VoipCallState,
    val muted: Boolean = false,
    val onHold: Boolean = false,
    val startedAt: Long = System.currentTimeMillis()
)
