package com.jdailer.feature.call.spam

import com.jdailer.core.common.coroutines.DispatcherProvider
import com.jdailer.core.database.dao.SpamDao
import kotlinx.coroutines.withContext

class DefaultSpamFilterService(
    private val spamDao: SpamDao,
    private val dispatchers: DispatcherProvider
) : SpamFilterService {
    override suspend fun classify(normalizedNumber: String): SpamDecision = withContext(dispatchers.io) {
        val profile = spamDao.getProfile(normalizedNumber) ?: return@withContext SpamDecision(
            isAllowed = true,
            shouldWarn = false,
            isBlocked = false
        )
        return@withContext when {
            profile.shouldBlock -> SpamDecision(isAllowed = false, shouldWarn = false, isBlocked = true, reason = profile.reason)
            profile.confidenceScore >= 70 -> SpamDecision(isAllowed = true, shouldWarn = true, isBlocked = false, reason = profile.reason)
            else -> SpamDecision(isAllowed = true, shouldWarn = false, isBlocked = false, reason = profile.reason)
        }
    }

    override suspend fun upsertProfile(profile: com.jdailer.core.database.entity.SpamProfileEntity) {
        withContext(dispatchers.io) {
            spamDao.upsert(profile)
        }
    }
}
