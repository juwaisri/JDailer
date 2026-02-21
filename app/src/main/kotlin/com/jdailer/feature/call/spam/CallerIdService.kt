package com.jdailer.feature.call.spam

interface CallerIdService {
    suspend fun evaluate(number: String): CallerIdDecision
    suspend fun block(number: String, blocked: Boolean)
    suspend fun upsertDecision(decision: CallerIdDecision)
}
