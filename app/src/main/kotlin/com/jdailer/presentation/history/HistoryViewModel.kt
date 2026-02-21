package com.jdailer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.history.domain.model.UnifiedHistoryItem
import com.jdailer.feature.history.domain.usecase.GetUnifiedHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class HistoryViewModel(
    private val getUnifiedHistoryUseCase: GetUnifiedHistoryUseCase
) : ViewModel() {
    private val _sources = MutableStateFlow<Set<HistorySource>>(emptySet())
    val selectedSources: StateFlow<Set<HistorySource>> = _sources.asStateFlow()

    val pagedHistory: Flow<PagingData<UnifiedHistoryItem>> = _sources
        .flatMapLatest { source ->
            getUnifiedHistoryUseCase(source, limit = 40)
        }
        .cachedIn(viewModelScope)

    fun setSources(sources: Set<HistorySource>) {
        _sources.value = sources
    }

    fun clearFilters() {
        _sources.value = emptySet()
    }
}

