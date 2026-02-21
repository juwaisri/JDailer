package com.jdailer.feature.call.spam

import com.jdailer.core.database.entity.SpamProfileEntity

data class SpamDecision(
    val isAllowed: Boolean,
    val shouldWarn: Boolean,
    val isBlocked: Boolean,
    val reason: String? = null
)

interface SpamFilterService {
    suspend fun classify(normalizedNumber: String): SpamDecision
    suspend fun upsertProfile(profile: SpamProfileEntity)
}
