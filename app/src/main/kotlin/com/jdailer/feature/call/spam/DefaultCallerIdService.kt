package com.jdailer.feature.call.spam

import android.content.Context
import android.util.LruCache
import com.jdailer.core.common.coroutines.DispatcherProvider
import com.jdailer.core.database.entity.CallerLookupEntity
import com.jdailer.core.database.dao.CallerLookupDao
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.hours

class DefaultCallerIdService(
    private val context: Context,
    private val dao: CallerLookupDao,
    private val dispatcher: DispatcherProvider,
    private val remoteCallerIdProvider: RemoteCallerIdProvider
) : CallerIdService {
    private val inMemoryCache = LruCache<String, CallerIdDecision>(100)
    private val cacheTtlMs = 24.hours.inWholeMilliseconds

    override suspend fun evaluate(number: String): CallerIdDecision = withContext(dispatcher.io) {
        val normalized = normalizeNumber(number)
        inMemoryCache.get(normalized)?.let { return@withContext it }

        val local = dao.getByNumber(normalized)
        if (local != null && !isStale(local.lastCheckedAt)) {
            val decision = local.toDecision(normalized)
            inMemoryCache.put(normalized, decision)
            return@withContext decision
        }

        val remote = runCatching { remoteCallerIdProvider.lookup(normalized) }.getOrNull()
        if (remote != null) {
            val decision = remote.toDecision(normalized)
            upsertDecision(decision)
            inMemoryCache.put(normalized, decision)
            return@withContext decision
        }

        if (local != null) {
            val fallback = local.toDecision(normalized)
            inMemoryCache.put(normalized, fallback)
            return@withContext fallback
        }

        val empty = CallerIdDecision(
            normalizedNumber = normalized,
            shouldAllow = true,
            shouldWarn = false,
            isBlocked = false,
            displayName = null,
            city = null,
            carrier = null,
            reason = "Caller profile not available",
            spamScore = 0
        )
        upsertDecision(empty)
        inMemoryCache.put(normalized, empty)
        empty
    }

    override suspend fun block(number: String, blocked: Boolean) {
        val normalized = normalizeNumber(number)
        withContext(dispatcher.io) {
            val entry = dao.getByNumber(normalized)
            if (entry == null) {
                dao.upsert(
                    com.jdailer.core.database.entity.CallerLookupEntity(
                        normalizedNumber = normalized,
                        displayName = null,
                        city = null,
                        carrier = null,
                        isSpam = blocked,
                        spamScore = if (blocked) 100 else 0,
                        reason = "Manual block",
                        isUserBlocked = blocked,
                        lastCheckedAt = System.currentTimeMillis()
                    )
                )
            } else {
                dao.updateUserBlock(normalized, blocked, System.currentTimeMillis())
            }
            inMemoryCache.remove(normalized)
        }
    }

    override suspend fun upsertDecision(decision: CallerIdDecision) {
        withContext(dispatcher.io) {
            dao.upsert(
                com.jdailer.core.database.entity.CallerLookupEntity(
                    normalizedNumber = decision.normalizedNumber,
                    displayName = decision.displayName,
                    city = decision.city,
                    carrier = decision.carrier,
                    isSpam = decision.isBlocked,
                    spamScore = decision.spamScore,
                    reason = decision.reason,
                    isUserBlocked = false,
                    lastCheckedAt = System.currentTimeMillis()
                )
            )
            inMemoryCache.put(decision.normalizedNumber, decision)
        }
    }

    private fun CallerLookupEntity.toDecision(normalized: String): CallerIdDecision {
        return CallerIdDecision(
            normalizedNumber = normalized,
            shouldAllow = !(isSpam || isUserBlocked),
            shouldWarn = spamScore in 60..79,
            isBlocked = isSpam || isUserBlocked,
            displayName = displayName,
            city = city,
            carrier = carrier,
            reason = reason,
            spamScore = spamScore
        )
    }

    private fun CallerIdRemoteProfile.toDecision(normalized: String): CallerIdDecision {
        return CallerIdDecision(
            normalizedNumber = normalized,
            shouldAllow = spamScore < 80 && !isSpam,
            shouldWarn = spamScore in 60..79,
            isBlocked = isSpam || spamScore >= 80,
            displayName = displayName,
            city = city,
            carrier = carrier,
            reason = reason,
            spamScore = spamScore
        )
    }

    private fun isStale(lastCheckedAt: Long): Boolean {
        return lastCheckedAt < System.currentTimeMillis() - cacheTtlMs
    }

    private fun normalizeNumber(raw: String): String = raw.filter { it.isDigit() || it == '+' }
}
