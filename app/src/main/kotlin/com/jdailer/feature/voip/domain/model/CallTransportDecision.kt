package com.jdailer.feature.voip.domain.model

enum class CallTransportType {
    TELECOM,
    SIP,
    FALLBACK_DIAL,
    BLOCKED
}

data class TelecomEligibilityPolicy(
    val allowSipWhenEnabled: Boolean = true,
    val allowTelecomFallback: Boolean = true,
    val requireDefaultDialer: Boolean = true,
    val allowFallbackDial: Boolean = true,
    val requireValidAddress: Boolean = true
)

data class CallTransportDecision(
    val address: String,
    val transportType: CallTransportType,
    val reason: String,
    val useSipUri: Boolean = false,
    val routed: Boolean = false
)
