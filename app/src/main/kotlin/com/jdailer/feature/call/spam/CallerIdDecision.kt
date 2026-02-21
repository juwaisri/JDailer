package com.jdailer.feature.call.spam

data class CallerIdDecision(
    val normalizedNumber: String,
    val shouldAllow: Boolean,
    val shouldWarn: Boolean,
    val isBlocked: Boolean,
    val displayName: String?,
    val city: String?,
    val carrier: String?,
    val reason: String?,
    val spamScore: Int = 0
)
