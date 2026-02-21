package com.jdailer.feature.call.spam.domain.model

enum class CallerRiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class CallerRiskProfile(
    val normalizedNumber: String,
    val riskLevel: CallerRiskLevel,
    val spamScore: Int,
    val isBlocked: Boolean,
    val shouldWarn: Boolean,
    val reasons: List<String>,
    val action: String
)
