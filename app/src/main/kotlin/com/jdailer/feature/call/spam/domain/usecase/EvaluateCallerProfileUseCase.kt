package com.jdailer.feature.call.spam.domain.usecase

import com.jdailer.feature.call.spam.CallerIdDecision
import com.jdailer.feature.call.spam.CallerIdService
import com.jdailer.feature.call.spam.SpamFilterService

class EvaluateCallerProfileUseCase(
    private val callerIdService: CallerIdService,
    private val spamFilterService: SpamFilterService
) {
    suspend operator fun invoke(number: String): CallerIdDecision {
        val normalized = number.filter { it.isDigit() || it == '+' }
        val callerDecision = callerIdService.evaluate(normalized)
        val spam = spamFilterService.classify(normalized)

        return callerDecision.copy(
            shouldAllow = callerDecision.shouldAllow && !spam.isBlocked,
            shouldWarn = spam.shouldWarn || callerDecision.shouldWarn,
            reason = listOfNotNull(callerDecision.reason, spam.reason).joinToString(" | ").ifBlank { null }
        )
    }
}
