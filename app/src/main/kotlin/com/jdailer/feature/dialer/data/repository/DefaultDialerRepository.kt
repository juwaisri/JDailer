package com.jdailer.feature.dialer.data.repository

import com.jdailer.core.common.coroutines.DispatcherProvider
import com.jdailer.core.database.dao.ContactDao
import com.jdailer.feature.dialer.data.DialerScoreCalculator
import com.jdailer.feature.dialer.data.T9Query
import com.jdailer.feature.dialer.domain.model.DialerSuggestion
import com.jdailer.feature.dialer.domain.repository.DialerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class DefaultDialerRepository(
    private val contactDao: ContactDao,
    private val dispatchers: DispatcherProvider,
    private val scoreCalculator: DialerScoreCalculator = DialerScoreCalculator(),
) : DialerRepository {
    override fun observeSuggestions(query: String, limit: Int): Flow<List<DialerSuggestion>> {
        val normalizedQuery = T9Query.normalizeName(query)
        val normalizedPattern = "%$normalizedQuery%"

        return if (normalizedQuery.isBlank()) {
            contactDao.observeRecentSuggestions(limit).map { rows ->
                rows.map { scoreCalculator.toSuggestion(it, query) }
            }
        } else {
            contactDao.observeSuggestions(normalizedPattern, limit).map { suggestions ->
                suggestions.map { scoreCalculator.toSuggestion(it, query) }
            }
        }.map { suggestions ->
            suggestions.sortedByDescending { it.score }
        }.flowOn(dispatchers.io)
    }
}
