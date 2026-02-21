package com.jdailer.feature.voip.domain.model

enum class VoipCallState {
    IDLE,
    RINGING,
    CONNECTING,
    ACTIVE,
    HOLDING,
    DISCONNECTED,
    FAILED
}
