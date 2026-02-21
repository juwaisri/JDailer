package com.jdailer.feature.dialer.domain.usecase

import com.jdailer.feature.dialer.domain.model.DialerSuggestion
import com.jdailer.feature.dialer.domain.repository.DialerRepository
import kotlinx.coroutines.flow.Flow

class ObserveDialSuggestionsUseCase(
    private val dialerRepository: DialerRepository
) {
    operator fun invoke(query: String, limit: Int = 8): Flow<List<DialerSuggestion>> {
        return dialerRepository.observeSuggestions(query, limit)
    }
}
