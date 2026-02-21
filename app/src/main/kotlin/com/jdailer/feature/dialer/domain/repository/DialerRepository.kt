package com.jdailer.feature.dialer.domain.repository

import com.jdailer.feature.dialer.domain.model.DialerSuggestion
import kotlinx.coroutines.flow.Flow

interface DialerRepository {
    fun observeSuggestions(query: String, limit: Int = 8): Flow<List<DialerSuggestion>>
}
