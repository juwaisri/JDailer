package com.jdailer.feature.call.spam.domain.usecase

import com.jdailer.feature.call.spam.CallerIdDecision
import com.jdailer.feature.call.spam.domain.usecase.EvaluateCallerProfileUseCase
import com.jdailer.feature.call.spam.domain.model.CallerRiskLevel
import com.jdailer.feature.call.spam.domain.model.CallerRiskProfile

class EvaluateCallerRiskUseCase(
    private val evaluateCallerProfileUseCase: EvaluateCallerProfileUseCase
) {
    suspend operator fun invoke(number: String): CallerRiskProfile {
        val normalized = number.filter { it.isDigit() || it == '+' }
        val decision: CallerIdDecision = evaluateCallerProfileUseCase(normalized)

        val reasons = listOfNotNull(decision.reason).toMutableList()
        if (decision.city != null) reasons.add("city=${decision.city}")
        if (decision.carrier != null) reasons.add("carrier=${decision.carrier}")

        val risk = when {
            !decision.shouldAllow -> CallerRiskLevel.CRITICAL
            decision.spamScore >= 80 -> CallerRiskLevel.HIGH
            decision.spamScore >= 45 -> CallerRiskLevel.MEDIUM
            else -> CallerRiskLevel.LOW
        }

        val shouldWarn = decision.shouldWarn || risk == CallerRiskLevel.HIGH

        return CallerRiskProfile(
            normalizedNumber = normalized,
            riskLevel = risk,
            spamScore = decision.spamScore,
            isBlocked = decision.isBlocked,
            shouldWarn = shouldWarn,
            reasons = reasons.distinct(),
            action = when (risk) {
                CallerRiskLevel.CRITICAL -> "block"
                CallerRiskLevel.HIGH -> "warn"
                else -> "allow"
            }
        )
    }
}

